package it.corso.ai.tappa10_microchat;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

/**
 * Registro delle chiamate di tool, una "traccia" per ogni richiesta in corso.
 *
 * <p>I tool vengono eseguiti dentro il {@code ToolCallingAdvisor}, lontano dal controller. Per far arrivare
 * alla pagina l'informazione "è stato chiamato giacenza(MOUSE-01)" si passa al tool un <b>id di richiesta</b>
 * (tramite il {@code toolContext}) e il tool scrive qui; il controller legge lo stesso flusso e lo inoltra
 * come evento SSE. Nel {@code toolContext} passa solo una stringa: niente oggetti nel payload verso il provider.
 */
@Component
public class TracciaStrumenti {

    /** Chiave dell'id di richiesta dentro il {@code toolContext}. */
    public static final String ID_RICHIESTA = "idRichiesta";

    private final Map<String, Sinks.Many<ChiamataStrumento>> aperte = new ConcurrentHashMap<>();

    /** Apre la traccia di una richiesta e restituisce il flusso delle chiamate di tool. */
    public Flux<ChiamataStrumento> apri(String idRichiesta) {
        Sinks.Many<ChiamataStrumento> sink = Sinks.many().unicast().onBackpressureBuffer();
        aperte.put(idRichiesta, sink);
        return sink.asFlux();
    }

    void registra(String idRichiesta, ChiamataStrumento chiamata) {
        Sinks.Many<ChiamataStrumento> sink = aperte.get(idRichiesta);
        if (sink != null) {
            // i tool possono girare su thread diversi: in caso di conflitto si ritenta per poco
            sink.emitNext(chiamata, Sinks.EmitFailureHandler.busyLooping(Duration.ofMillis(200)));
        }
    }

    /** Chiude la traccia: va chiamato a fine risposta, anche in caso di errore o annullamento. */
    public void chiudi(String idRichiesta) {
        Sinks.Many<ChiamataStrumento> sink = aperte.remove(idRichiesta);
        if (sink != null) {
            sink.tryEmitComplete();
        }
    }

    /** Una chiamata di tool, così come la vede la pagina. */
    public record ChiamataStrumento(String nome, String argomenti, String risultato) {}
}

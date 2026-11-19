package it.corso.ai.esercizi.parte1;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import it.corso.ai.esercizi.DaSvolgere;
import reactor.core.publisher.Flux;

/**
 * <h3>Parte 1 — Esercizio Difficile</h3>
 *
 * <p><b>Obiettivo:</b> creare un endpoint {@code POST /ai/riassunto} che restituisce in streaming un riassunto
 * in 3 punti elenco di un testo ricevuto nel body.
 *
 * <p><b>Dati:</b> body di prova (testo libero di almeno 8 righe a scelta); risposta come {@code text/event-stream};
 * il system prompt deve imporre l'italiano e un massimo di 3 punti.
 *
 * <p><b>Risultato atteso:</b> con
 * {@code curl -N -X POST -H "Content-Type: text/plain" --data-binary @testo.txt http://localhost:8080/ai/riassunto}
 * i punti compaiono progressivamente nel terminale. Se spegni la connessione di rete o usi una chiave errata,
 * l'ultimo pezzo ricevuto è un messaggio di errore leggibile e non una risposta vuota.
 *
 * <p><b>Ripasso:</b> tappa 3 ({@code StoriaController}).
 */
@SuppressWarnings("unused")   // i campi verranno usati nella tua soluzione
@RestController
@Profile("!soluzioni")
@RequestMapping("/ai")
class RiassuntoEsercizio {

    private final ChatClient chatClient;

    RiassuntoEsercizio(ChatClient.Builder builder) {
        // TODO: system prompt con italiano e massimo 3 punti elenco
        this.chatClient = builder.build();
    }

    @PostMapping(value = "/riassunto", consumes = MediaType.TEXT_PLAIN_VALUE,
            produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    Flux<String> riassunto(@RequestBody String testo) {
        // TODO: stream() + content() + onErrorResume(...)
        throw DaSvolgere.esercizio("Parte 1 Difficile - /ai/riassunto");
    }
}

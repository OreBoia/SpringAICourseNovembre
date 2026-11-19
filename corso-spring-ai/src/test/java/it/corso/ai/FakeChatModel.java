package it.corso.ai;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;

import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.metadata.ChatResponseMetadata;
import org.springframework.ai.chat.metadata.DefaultUsage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.tool.ToolCallingChatOptions;

import reactor.core.publisher.Flux;

/**
 * Un modello di chat <b>finto</b>: nessuna rete, nessuna chiave, risposte decise dal test.
 *
 * <p>Idea da mostrare in aula: il {@code ChatClient} parla con un'interfaccia ({@code ChatModel}).
 * Sostituendo l'implementazione possiamo testare prompt, template, memoria, conversione in oggetti
 * e perfino il ciclo dei tool in modo deterministico. Ogni prompt ricevuto viene registrato per poterlo ispezionare.
 */
public class FakeChatModel implements ChatModel {

    /** Decide la risposta del modello finto, a partire dal prompt ricevuto. */
    @FunctionalInterface
    public interface Copione {
        AssistantMessage rispondi(Prompt prompt);
    }

    private final Copione copione;
    private final List<Prompt> promptRicevuti = new CopyOnWriteArrayList<>();

    /** Risponde con il testo calcolato dalla funzione. */
    public FakeChatModel(Function<Prompt, String> risponditore) {
        this.copione = prompt -> new AssistantMessage(risponditore.apply(prompt));
    }

    private FakeChatModel(Copione copione) {
        this.copione = copione;
    }

    /** Risponde sempre con lo stesso testo. */
    public static FakeChatModel cheRisponde(String testo) {
        return new FakeChatModel((Copione) _ -> new AssistantMessage(testo));
    }

    /** Simula un provider che non risponde (chiave sbagliata, rete assente, quota esaurita). */
    public static FakeChatModel cheFallisce() {
        return new FakeChatModel((Copione) _ -> {
            throw new IllegalStateException("provider non raggiungibile");
        });
    }

    /**
     * Risponde con un messaggio completo, che può contenere <b>richieste di tool</b>: serve a simulare il
     * ciclo del function calling (il modello chiede il tool, l'app lo esegue, il modello scrive la risposta).
     */
    public static FakeChatModel aCopione(Copione copione) {
        return new FakeChatModel(copione);
    }

    /** Un messaggio dell'assistente che chiede di eseguire un tool con questi argomenti JSON. */
    public static AssistantMessage chiedeTool(String nome, String argomentiJson) {
        return AssistantMessage.builder()
                .content("")
                .toolCalls(List.of(new AssistantMessage.ToolCall("call-1", "function", nome, argomentiJson)))
                .build();
    }

    /**
     * Come i modelli veri (es. {@code OpenAiChatOptions}), dichiara opzioni che supportano i tool:
     * senza, il {@code ChatClient} non attiverebbe il ciclo del function calling.
     */
    @Override
    public ChatOptions getOptions() {
        return ToolCallingChatOptions.builder().build();
    }

    @Override
    public ChatResponse call(Prompt prompt) {
        promptRicevuti.add(prompt);
        AssistantMessage risposta = copione.rispondi(prompt);
        return new ChatResponse(
                List.of(new Generation(risposta)),
                ChatResponseMetadata.builder().usage(new DefaultUsage(10, 5)).build());   // 15 token in totale
    }

    @Override
    public Flux<ChatResponse> stream(Prompt prompt) {
        return Flux.defer(() -> {
            promptRicevuti.add(prompt);
            AssistantMessage risposta = copione.rispondi(prompt);
            if (risposta.hasToolCalls()) {
                return Flux.just(new ChatResponse(List.of(new Generation(risposta))));
            }
            // spezza la risposta in parole, come farebbe un provider vero
            return Flux.fromArray(risposta.getText().split("(?<= )"))
                    .map(pezzo -> new ChatResponse(List.of(new Generation(new AssistantMessage(pezzo)))));
        });
    }

    public List<Prompt> promptRicevuti() {
        return promptRicevuti;
    }

    public Prompt ultimoPrompt() {
        return promptRicevuti.getLast();
    }
}

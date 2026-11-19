package it.corso.ai.tappa10_microchat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.document.Document;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;

import it.corso.ai.tappa05_tools.MagazzinoTools;
import reactor.core.publisher.Flux;

/**
 * Il "cervello" della micro chat: un {@code ChatClient} con memoria, tools e (se c'è il profilo {@code rag}) RAG.
 *
 * <p>Il {@code ChatClient} è costruito qui e non esposto come bean: nel contesto resta un solo bean
 * {@code ChatClient} (l'assistente della tappa 6), quindi nessuna ambiguità nelle iniezioni.
 */
@Service
public class MicroChat {

    /** Finestra del {@code MessageWindowChatMemory} auto-configurato. */
    static final int FINESTRA = 20;

    /** Dopo la memoria (MIN+200) e prima del ciclo dei tool (MIN+300): una sola ricerca per domanda. */
    static final int ORDINE_RAG = Advisor.DEFAULT_CHAT_MEMORY_PRECEDENCE_ORDER + 50;

    static final String ISTRUZIONI = """
            Sei l'assistente interno di un negozio di informatica. Rispondi sempre in italiano, in modo breve.
            - Per giacenze e prenotazioni del magazzino usa SOLO i tool: non inventare mai numeri.
            - Per le regole aziendali (ferie, rimborsi, lavoro da remoto, password, assistenza IT) usa solo
              le informazioni del contesto, se presente; se non c'è, di' che non lo sai.
            - Se la domanda non riguarda il magazzino né le regole aziendali, rispondi normalmente.
            """;

    private final ChatClient client;
    private final ChatMemory memoria;
    private final TracciaStrumenti traccia;
    private final boolean ragAttivo;

    public MicroChat(ChatClient.Builder builder, ChatMemory memoria, MagazzinoTools magazzino,
                     TracciaStrumenti traccia, Optional<VectorStore> regolamento) {
        this.memoria = memoria;
        this.traccia = traccia;
        this.ragAttivo = regolamento.isPresent();

        // I metodi @Tool di MagazzinoTools diventano ToolCallback, ognuno avvolto dal decoratore che lo traccia
        ToolCallback[] strumenti = Arrays.stream(
                        MethodToolCallbackProvider.builder().toolObjects(magazzino).build().getToolCallbacks())
                .map(vero -> new StrumentoTracciato(vero, traccia))
                .toArray(ToolCallback[]::new);

        List<Advisor> advisors = new ArrayList<>();
        advisors.add(MessageChatMemoryAdvisor.builder(memoria).build());
        regolamento.ifPresent(store -> advisors.add(QuestionAnswerAdvisor.builder(store)
                .searchRequest(SearchRequest.builder().topK(2).similarityThreshold(0.4).build())
                .order(ORDINE_RAG)
                .build()));

        this.client = builder
                .defaultSystem(ISTRUZIONI)
                .defaultAdvisors(advisors)
                .defaultTools((Object[]) strumenti)   // in 2.0 defaultTools accetta anche ToolCallback già pronti
                .build();
    }

    /**
     * Risponde in streaming. Gli eventi SSE, nell'ordine in cui possono arrivare:
     * {@code strumento} (0..n), {@code fonti} (0..1), {@code testo} (1..n), {@code errore} (0..1), {@code fine} (1).
     */
    public Flux<ServerSentEvent<Object>> rispondi(String sessione, String messaggio) {
        String idRichiesta = UUID.randomUUID().toString();
        Flux<ServerSentEvent<Object>> strumenti = traccia.apri(idRichiesta).map(c -> evento("strumento", c));

        AtomicBoolean fontiInviate = new AtomicBoolean();
        Flux<ServerSentEvent<Object>> risposta = client.prompt()
                .user(messaggio)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, sessione))   // l'ID lo decide il server
                .toolContext(Map.of(TracciaStrumenti.ID_RICHIESTA, idRichiesta))
                .stream()
                .chatClientResponse()            // non solo il testo: serve anche il contesto (fonti del RAG)
                .concatMap(r -> Flux.fromIterable(eventi(r, fontiInviate)))
                .doFinally(_ -> traccia.chiudi(idRichiesta));

        return Flux.merge(strumenti, risposta)
                // come nella tappa 3: lo status HTTP è già 200, l'errore diventa un evento leggibile
                .onErrorResume(e -> Flux.just(evento("errore", new Errore(MessaggiErrore.spiega(e)))))
                // calcolato a fine flusso, dopo che la memoria ha salvato domanda e risposta
                .concatWith(Flux.defer(() -> Flux.just(evento("fine", stato(sessione)))));
    }

    /** Messaggi utente/assistente della conversazione, così come li conserva la memoria. */
    public List<Battuta> storia(String sessione) {
        return memoria.get(sessione).stream()
                .filter(m -> m.getMessageType() == MessageType.USER || m.getMessageType() == MessageType.ASSISTANT)
                .map(m -> new Battuta(m.getMessageType() == MessageType.USER ? "utente" : "assistente", m.getText()))
                .toList();
    }

    /** "Nuova conversazione": svuota la memoria della sessione. */
    public void dimentica(String sessione) {
        memoria.clear(sessione);
    }

    public StatoMemoria stato(String sessione) {
        return new StatoMemoria(memoria.get(sessione).size(), FINESTRA);
    }

    public boolean ragAttivo() {
        return ragAttivo;
    }

    private static List<ServerSentEvent<Object>> eventi(ChatClientResponse r, AtomicBoolean fontiInviate) {
        List<ServerSentEvent<Object>> eventi = new ArrayList<>(2);
        // QuestionAnswerAdvisor mette i documenti recuperati nel contesto della risposta
        if (!fontiInviate.get()
                && r.context().get(QuestionAnswerAdvisor.RETRIEVED_DOCUMENTS) instanceof List<?> documenti
                && !documenti.isEmpty()) {
            fontiInviate.set(true);
            List<String> fonti = documenti.stream()
                    .filter(Document.class::isInstance)
                    .map(d -> ((Document) d).getText())
                    .toList();
            eventi.add(evento("fonti", fonti));
        }
        String testo = testo(r);
        if (testo != null && !testo.isEmpty()) {
            eventi.add(evento("testo", new Pezzo(testo)));
        }
        return eventi;
    }

    private static String testo(ChatClientResponse r) {
        if (r.chatResponse() == null || r.chatResponse().getResult() == null) {
            return null;
        }
        Message output = r.chatResponse().getResult().getOutput();
        return output == null ? null : output.getText();
    }

    private static ServerSentEvent<Object> evento(String nome, Object dati) {
        return ServerSentEvent.<Object>builder(dati).event(nome).build();
    }

    /** Il testo va dentro un oggetto JSON: così spazi iniziali e "a capo" arrivano intatti al browser. */
    public record Pezzo(String testo) {}

    public record Errore(String messaggio) {}

    public record StatoMemoria(int messaggiInMemoria, int finestra) {}

    public record Battuta(String ruolo, String testo) {}
}

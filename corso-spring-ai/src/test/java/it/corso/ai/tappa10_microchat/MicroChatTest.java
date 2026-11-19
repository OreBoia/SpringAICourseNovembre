package it.corso.ai.tappa10_microchat;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.Embedding;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.http.codec.ServerSentEvent;

import it.corso.ai.FakeChatModel;
import it.corso.ai.tappa05_tools.MagazzinoTools;
import it.corso.ai.tappa10_microchat.MicroChat.Errore;
import it.corso.ai.tappa10_microchat.MicroChat.Pezzo;
import it.corso.ai.tappa10_microchat.MicroChat.StatoMemoria;
import it.corso.ai.tappa10_microchat.TracciaStrumenti.ChiamataStrumento;

/**
 * La micro chat senza rete: il modello finto segue un copione che chiede un tool e poi risponde.
 * Così si verificano davvero il ciclo dei tool, la memoria, l'ordine degli advisor e il RAG.
 */
class MicroChatTest {

    /** Se l'ultimo messaggio è il risultato di un tool, risponde; altrimenti chiede la giacenza di MOUSE-01. */
    private static final FakeChatModel.Copione GIACENZA = prompt -> {
        Message ultimo = prompt.getInstructions().getLast();
        if (ultimo instanceof ToolResponseMessage risultato) {
            return new AssistantMessage("Di MOUSE-01 ne abbiamo " + risultato.getResponses().getFirst().responseData() + ".");
        }
        return FakeChatModel.chiedeTool("giacenza", "{\"codice\":\"MOUSE-01\"}");
    };

    private final ChatMemory memoria = MessageWindowChatMemory.builder().build();

    private MicroChat chat(FakeChatModel modello, Optional<VectorStore> regolamento) {
        return new MicroChat(ChatClient.builder(modello), memoria, new MagazzinoTools(), new TracciaStrumenti(), regolamento);
    }

    private static List<ServerSentEvent<Object>> eventi(MicroChat chat, String sessione, String messaggio) {
        return chat.rispondi(sessione, messaggio).collectList().block();
    }

    private static List<String> nomi(List<ServerSentEvent<Object>> eventi) {
        return eventi.stream().map(e -> e.event()).toList();
    }

    private static String testo(List<ServerSentEvent<Object>> eventi) {
        StringBuilder sb = new StringBuilder();
        eventi.stream().filter(e -> "testo".equals(e.event())).forEach(e -> sb.append(((Pezzo) e.data()).testo()));
        return sb.toString();
    }

    @Test
    void ilToolVieneEseguitoEMostratoPrimaDelTesto() {
        MicroChat chat = chat(FakeChatModel.aCopione(GIACENZA), Optional.empty());

        List<ServerSentEvent<Object>> eventi = eventi(chat, "s1", "Quanti mouse abbiamo?");

        assertThat(nomi(eventi).getFirst()).isEqualTo("strumento");
        assertThat(nomi(eventi).getLast()).isEqualTo("fine");
        assertThat(eventi.getFirst().data())
                .isEqualTo(new ChiamataStrumento("giacenza", "{\"codice\":\"MOUSE-01\"}", "25"));
        assertThat(testo(eventi)).isEqualTo("Di MOUSE-01 ne abbiamo 25.");
    }

    @Test
    void laMemoriaSalvaSoloDomandaERispostaFinale() {
        FakeChatModel modello = FakeChatModel.aCopione(GIACENZA);
        MicroChat chat = chat(modello, Optional.empty());

        List<ServerSentEvent<Object>> eventi = eventi(chat, "s1", "Quanti mouse abbiamo?");

        // niente messaggi di tool in memoria: Groq rifiuterebbe una storia con tool call senza risposta
        assertThat(memoria.get("s1")).extracting(m -> m.getMessageType())
                .containsExactly(MessageType.USER, MessageType.ASSISTANT);
        assertThat(eventi.getLast().data()).isEqualTo(new StatoMemoria(2, MicroChat.FINESTRA));

        eventi(chat, "s1", "E le tastiere?");
        long quante = modello.ultimoPrompt().getInstructions().stream()
                .filter(m -> "Quanti mouse abbiamo?".equals(m.getText()))
                .count();
        assertThat(quante).as("la storia compare una sola volta nel prompt").isEqualTo(1);
    }

    @Test
    void sessioniDiverseENuovaConversazione() {
        MicroChat chat = chat(FakeChatModel.cheRisponde("Ciao!"), Optional.empty());

        eventi(chat, "anna", "Mi chiamo Anna");
        assertThat(chat.storia("anna")).hasSize(2);
        assertThat(chat.storia("luca")).isEmpty();

        chat.dimentica("anna");

        assertThat(chat.storia("anna")).isEmpty();
        assertThat(chat.stato("anna").messaggiInMemoria()).isZero();
    }

    @Test
    void unErroreDelProviderDiventaUnEventoLeggibile() {
        MicroChat chat = chat(FakeChatModel.cheFallisce(), Optional.empty());

        List<ServerSentEvent<Object>> eventi = eventi(chat, "s1", "Ciao");

        assertThat(nomi(eventi)).containsExactly("errore", "fine");
        assertThat(((Errore) eventi.getFirst().data()).messaggio()).startsWith("Errore del provider AI");
    }

    @Test
    void conIlRagArrivanoLeFontiELaMemoriaSalvaLaDomandaOriginale() {
        VectorStore regolamento = SimpleVectorStore.builder(new EmbeddingPerParoleChiave()).build();
        regolamento.add(List.of(
                new Document("La password scade ogni 90 giorni e deve avere almeno 12 caratteri."),
                new Document("Le ferie vanno richieste almeno 10 giorni prima tramite il portale HR.")));
        FakeChatModel modello = FakeChatModel.cheRisponde("Ogni 90 giorni.");
        MicroChat chat = chat(modello, Optional.of(regolamento));

        List<ServerSentEvent<Object>> eventi = eventi(chat, "s1", "Ogni quanto scade la password?");

        assertThat(nomi(eventi)).contains("fonti");
        assertThat(eventi.stream().filter(e -> "fonti".equals(e.event())).findFirst().orElseThrow().data())
                .isEqualTo(List.of("La password scade ogni 90 giorni e deve avere almeno 12 caratteri."));
        // il modello ha ricevuto il contesto...
        assertThat(modello.ultimoPrompt().getUserMessage().getText()).contains("90 giorni");
        // ...ma la memoria conserva la domanda così come l'ha scritta l'utente (advisor RAG dopo la memoria)
        assertThat(memoria.get("s1").getFirst().getText()).isEqualTo("Ogni quanto scade la password?");
    }

    /** Embedding finto: una dimensione per parola chiave. Basta per avere similarità deterministiche. */
    static class EmbeddingPerParoleChiave implements EmbeddingModel {

        private static final List<String> PAROLE = List.of("password", "ferie", "mouse");

        @Override
        public EmbeddingResponse call(EmbeddingRequest richiesta) {
            List<String> testi = richiesta.getInstructions();
            return new EmbeddingResponse(IntStream.range(0, testi.size())
                    .mapToObj(i -> new Embedding(vettore(testi.get(i)), i))
                    .toList());
        }

        @Override
        public float[] embed(Document documento) {
            return vettore(documento.getText());
        }

        private static float[] vettore(String testo) {
            float[] v = new float[PAROLE.size() + 1];
            for (int i = 0; i < PAROLE.size(); i++) {
                v[i] = testo.toLowerCase().contains(PAROLE.get(i)) ? 1f : 0f;
            }
            v[PAROLE.size()] = 0.1f;   // evita vettori nulli
            return v;
        }
    }

    @Test
    void gliErroriDiGroqVengonoTradotti() {
        assertThat(MessaggiErrore.spiega(new RuntimeException("HTTP 401 Unauthorized: Invalid API Key")))
                .startsWith("Chiave Groq mancante o non valida");
        assertThat(MessaggiErrore.spiega(new RuntimeException("wrapper", new RuntimeException("429 Too Many Requests: rate limit"))))
                .startsWith("Limite del piano gratuito");
        assertThat(MessaggiErrore.spiega(new RuntimeException("400 {\"error\":{\"code\":\"tool_use_failed\"}}")))
                .startsWith("Il modello ha generato una chiamata di tool non valida");
        assertThat(MessaggiErrore.spiega(new RuntimeException("The model `llama-x` does not exist")))
                .startsWith("Il modello configurato non è disponibile");
    }
}

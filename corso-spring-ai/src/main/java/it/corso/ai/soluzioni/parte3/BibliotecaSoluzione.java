package it.corso.ai.soluzioni.parte3;

import java.util.List;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Soluzione Parte 3 — Difficile: FAQ della biblioteca con RAG e fonti.
 *
 * <p>Le fonti si ottengono ripetendo la ricerca con lo <b>stesso</b> {@link SearchRequest} dell'advisor.
 * Se la ricerca è vuota si risponde "Non lo so" <b>senza chiamare il modello</b>: è l'unico modo deterministico.
 */
@RestController
@Profile("rag & soluzioni")
@RequestMapping("/biblioteca")
class BibliotecaSoluzione {

    static final String NON_LO_SO = "Non lo so";

    // Calibrazione (punteggi misurati con il profilo rag-multilingua, vedi README "Calibrazione"):
    //   "Fino a quando posso tenere un libro?"  → prestito 0.46, rinnovo 0.42   → servono 2 passaggi: topK 2
    //   "Quanto pago se restituisco in ritardo?" → ritardo 0.29 (tessera 0.30)  → la soglia deve stare sotto 0.28
    //   "Avete il servizio di fotocopie?"        → massimo 0.21                 → la soglia deve stare sopra 0.21
    // Soglia 0.25: in mezzo tra il passaggio giusto più basso e quello sbagliato più alto.
    // Con il modello predefinito (solo inglese) i due gruppi si sovrappongono e nessuna soglia funziona.
    static final int TOP_K = 2;
    static final double SOGLIA = 0.25;

    private final VectorStore store;
    private final SearchRequest ricerca = SearchRequest.builder()
            .topK(TOP_K)
            .similarityThreshold(SOGLIA)
            .build();
    private final ChatClient chatClient;

    BibliotecaSoluzione(EmbeddingModel embeddingModel, ChatClient.Builder builder) {
        // Store creato qui e non come bean: nel contesto c'è già il VectorStore del regolamento (tappa 9)
        this.store = SimpleVectorStore.builder(embeddingModel).build();
        this.store.add(List.of(
                new Document("Orari: lunedì-venerdì 9-19, sabato 9-13."),
                new Document("La tessera permette il prestito di 5 libri per 30 giorni."),
                new Document("Il prestito si rinnova una sola volta, se nessuno ha prenotato il libro."),
                new Document("Il ritardo costa 0,50 euro al giorno per ogni libro."),
                new Document("La tessera è gratuita e si fa in sede con un documento d'identità."),
                new Document("La sala studio ha 40 posti e si prenota dal sito fino a 2 giorni prima.")));

        this.chatClient = builder
                .defaultSystem("Sei l'assistente di una biblioteca. Rispondi in italiano solo in base al contesto "
                        + "fornito. Se la risposta non è nel contesto, rispondi esattamente: " + NON_LO_SO + ".")
                .defaultAdvisors(QuestionAnswerAdvisor.builder(store).searchRequest(ricerca).build())
                .build();
    }

    @GetMapping("/chiedi")
    RispostaConFonti chiedi(@RequestParam String domanda) {
        List<String> fonti = store.similaritySearch(SearchRequest.from(ricerca).query(domanda).build())
                .stream()
                .map(d -> d.getText())
                .toList();

        if (fonti.isEmpty()) {
            return new RispostaConFonti(NON_LO_SO, fonti);   // nessuna chiamata al modello
        }
        String risposta = chatClient.prompt().user(domanda).call().content();
        return new RispostaConFonti(risposta, fonti);
    }

    record RispostaConFonti(String risposta, List<String> fonti) {}
}

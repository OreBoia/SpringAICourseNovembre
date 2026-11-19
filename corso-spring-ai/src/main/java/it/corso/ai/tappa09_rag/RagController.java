package it.corso.ai.tappa09_rag;

import java.util.List;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Profile("rag")
@RequestMapping("/rag")
class RagController {

    private final VectorStore vectorStore;
    private final ChatClient chatClient;

    RagController(VectorStore vectorStore, ChatClient.Builder builder) {
        this.vectorStore = vectorStore;
        this.chatClient = builder
                .defaultSystem("Rispondi solo in base al contesto fornito. "
                        + "Se la risposta non è nel contesto, rispondi esattamente: Non lo so.")
                // L'advisor cerca nello store e aggiunge i testi trovati al prompt
                .defaultAdvisors(QuestionAnswerAdvisor.builder(vectorStore)
                        .searchRequest(SearchRequest.builder()
                                .similarityThreshold(0.4d)   // scarta i risultati poco simili
                                .topK(2)                     // al massimo 2 passaggi
                                .build())
                        .build())
                .build();
    }

    // Solo ricerca: nessuna chiamata al modello di chat
    @GetMapping("/cerca")
    List<Trovato> cerca(@RequestParam String q) {
        List<Document> trovati = vectorStore.similaritySearch(
                SearchRequest.builder().query(q).topK(3).build());
        return trovati.stream()
                .map(d -> new Trovato(d.getText(), d.getScore()))
                .toList();
    }

    // RAG completo: ricerca + prompt aumentato + risposta
    @GetMapping("/chiedi")
    String chiedi(@RequestParam String domanda) {
        return chatClient.prompt().user(domanda).call().content();
    }

    record Trovato(String testo, Double punteggio) {}
}

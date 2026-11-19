package it.corso.ai.tappa09_rag;

import java.util.List;
import java.util.Map;

import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("rag")
class RagConfig {

    // Store in memoria: usa l'EmbeddingModel locale per creare i vettori
    @Bean
    VectorStore vectorStore(EmbeddingModel embeddingModel) {
        return SimpleVectorStore.builder(embeddingModel).build();
    }

    // Ingestion: a ogni avvio carica i documenti (i testi sono già brevi: niente chunking)
    @Bean
    CommandLineRunner caricaRegolamento(VectorStore store) {
        return args -> store.add(List.of(
            new Document("Le ferie vanno richieste almeno 10 giorni prima tramite il portale HR.",
                    Map.of("argomento", "ferie")),
            new Document("I rimborsi spese si richiedono entro 30 giorni, allegando gli scontrini.",
                    Map.of("argomento", "rimborsi")),
            new Document("Il lavoro da remoto è consentito fino a 2 giorni a settimana, previo accordo con il responsabile.",
                    Map.of("argomento", "remoto")),
            new Document("La password scade ogni 90 giorni e deve avere almeno 12 caratteri.",
                    Map.of("argomento", "sicurezza")),
            new Document("Per l'assistenza IT si apre un ticket e si riceve risposta entro 4 ore lavorative.",
                    Map.of("argomento", "it"))));
    }
}

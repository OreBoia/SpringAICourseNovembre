package it.corso.ai.esercizi.parte3;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import it.corso.ai.esercizi.DaSvolgere;

/**
 * <h3>Parte 3 — Esercizio Difficile</h3> (attivo con il profilo {@code rag}: {@code -Dspring-boot.run.profiles=groq,rag})
 *
 * <p><b>Obiettivo:</b> realizzare le FAQ di una biblioteca con il RAG e un endpoint
 * {@code GET /biblioteca/chiedi?domanda=...} che restituisce le fonti usate.
 *
 * <p><b>Dati:</b> questi 6 documenti, in un {@code SimpleVectorStore} <b>creato dentro questo controller</b>
 * (non come bean: nel contesto c'è già il {@code VectorStore} del regolamento, tappa 9):
 * <ul>
 *   <li>Orari: lunedì-venerdì 9-19, sabato 9-13.</li>
 *   <li>La tessera permette il prestito di 5 libri per 30 giorni.</li>
 *   <li>Il prestito si rinnova una sola volta, se nessuno ha prenotato il libro.</li>
 *   <li>Il ritardo costa 0,50 euro al giorno per ogni libro.</li>
 *   <li>La tessera è gratuita e si fa in sede con un documento d'identità.</li>
 *   <li>La sala studio ha 40 posti e si prenota dal sito fino a 2 giorni prima.</li>
 * </ul>
 *
 * <p><b>Risultato atteso:</b> un JSON {@code {"risposta":"...","fonti":["...","..."]}}, dove {@code fonti} contiene
 * i testi dei documenti che superano la stessa soglia con lo stesso topK usati per la risposta. Tre domande di prova:
 * <ul>
 *   <li>{@code Fino a quando posso tenere un libro?} → 30 giorni, rinnovabile una sola volta (fonti: prestito e rinnovo);</li>
 *   <li>{@code Quanto pago se restituisco in ritardo?} → 0,50 euro al giorno per libro (fonte: ritardo);</li>
 *   <li>{@code Avete il servizio di fotocopie?} → {@code Non lo so}, con l'elenco delle fonti vuoto.</li>
 * </ul>
 * Scegli topK e soglia provando ({@code /rag/similarita} ti aiuta a vedere i punteggi) e giustifica in due righe i valori scelti.
 *
 * <p><b>Suggerimento:</b> se i punteggi delle domande pertinenti e di quella fuori tema si sovrappongono,
 * nessuna soglia funziona: prova ad aggiungere il profilo {@code rag-multilingua} e confronta.
 */
@SuppressWarnings("unused")   // i campi verranno usati nella tua soluzione
@RestController
@Profile("rag & !soluzioni")
@RequestMapping("/biblioteca")
class BibliotecaEsercizio {

    private final EmbeddingModel embeddingModel;
    private final ChatClient.Builder builder;

    BibliotecaEsercizio(EmbeddingModel embeddingModel, ChatClient.Builder builder) {
        this.embeddingModel = embeddingModel;
        this.builder = builder;
        // TODO: crea lo store con SimpleVectorStore.builder(embeddingModel).build() e carica i 6 documenti
    }

    @GetMapping("/chiedi")
    Object chiedi(@RequestParam String domanda) {
        // TODO: cerca le fonti, se vuote rispondi "Non lo so" senza chiamare il modello, altrimenti usa il RAG
        throw DaSvolgere.esercizio("Parte 3 Difficile - /biblioteca/chiedi");
    }
}

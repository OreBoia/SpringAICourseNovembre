package it.corso.ai.esercizi.parte2;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import it.corso.ai.esercizi.DaSvolgere;

/**
 * <h3>Parte 2 — Esercizio Facile</h3>
 *
 * <p><b>Obiettivo:</b> creare un endpoint {@code POST /ai/libro} che estrae i dati di un libro da una frase
 * e li restituisce come oggetto JSON.
 *
 * <p><b>Dati:</b> un record {@code Libro} con i campi {@code titolo}, {@code autore}, {@code anno}; la frase arriva
 * nel body come testo semplice. Frase di prova:
 * {@code Il nome della rosa è un romanzo di Umberto Eco pubblicato nel 1980.}
 *
 * <p><b>Risultato atteso:</b> {@code {"titolo":"Il nome della rosa","autore":"Umberto Eco","anno":1980}}.
 * Con {@code Se questo è un uomo è un libro di Primo Levi uscito nel 1947.} restituisce
 * {@code {"titolo":"Se questo è un uomo","autore":"Primo Levi","anno":1947}}.
 *
 * <p><b>Prova:</b>
 * {@code curl -X POST -H "Content-Type: text/plain" -d "Il nome della rosa è un romanzo di Umberto Eco pubblicato nel 1980." http://localhost:8080/ai/libro}
 *
 * <p><b>Ripasso:</b> tappa 4 ({@code .entity(...)}).
 */
@SuppressWarnings("unused")   // i campi verranno usati nella tua soluzione
@RestController
@Profile("!soluzioni")
@RequestMapping("/ai")
class LibroEsercizio {

    private final ChatClient chatClient;

    LibroEsercizio(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    // TODO: crea il record Libro e cambia il tipo di ritorno da Object a Libro
    @PostMapping(value = "/libro", consumes = MediaType.TEXT_PLAIN_VALUE)
    Object libro(@RequestBody String frase) {
        throw DaSvolgere.esercizio("Parte 2 Facile - /ai/libro");
    }
}

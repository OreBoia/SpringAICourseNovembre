package it.corso.ai.esercizi.parte1;

import java.util.Map;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import it.corso.ai.esercizi.DaSvolgere;

/**
 * <h3>Parte 1 — Esercizio Medio</h3>
 *
 * <p><b>Obiettivo:</b> creare un endpoint {@code GET /ai/commit} che migliora un messaggio di commit Git.
 *
 * <p><b>Dati:</b> il client deve avere un {@code defaultSystem} che lo definisce "revisore di commit message":
 * risposta in inglese, imperativo, massimo 72 caratteri, una sola riga.
 * Input di prova: {@code fix bug login che non funzionava}.
 *
 * <p><b>Risultato atteso:</b> JSON con due campi, {@code messaggio} (la riga proposta, es. {@code Fix login failure})
 * e {@code tokenTotali} (numero intero maggiore di 0).
 *
 * <p><b>Ripasso:</b> tappa 2: {@code defaultSystem} e {@code chatResponse()}.
 */
@SuppressWarnings("unused")   // i campi verranno usati nella tua soluzione
@RestController
@Profile("!soluzioni")
@RequestMapping("/ai")
class CommitEsercizio {

    private final ChatClient chatClient;

    CommitEsercizio(ChatClient.Builder builder) {
        // TODO: aggiungi al builder il defaultSystem del "revisore di commit message"
        this.chatClient = builder.build();
    }

    @GetMapping("/commit")
    Map<String, Object> commit(@RequestParam String messaggio) {
        // TODO: chiama il modello con chatResponse() e restituisci messaggio + tokenTotali
        throw DaSvolgere.esercizio("Parte 1 Medio - /ai/commit");
    }
}

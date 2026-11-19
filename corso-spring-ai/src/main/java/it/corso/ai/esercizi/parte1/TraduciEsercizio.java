package it.corso.ai.esercizi.parte1;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import it.corso.ai.esercizi.DaSvolgere;

/**
 * <h3>Parte 1 — Esercizio Facile</h3>
 *
 * <p><b>Obiettivo:</b> creare un endpoint {@code GET /ai/traduci} che traduce un testo nella lingua richiesta.
 *
 * <p><b>Dati:</b> parametri {@code testo} e {@code lingua}; il prompt deve usare un template con due segnaposto.
 *
 * <p><b>Risultato atteso:</b> {@code GET /ai/traduci?testo=Buongiorno a tutti&lingua=inglese} restituisce solo
 * la traduzione (es. {@code Good morning everyone}), senza spiegazioni aggiuntive.
 *
 * <p><b>Ripasso:</b> tappa 2 ({@code RipassoController}): {@code .user(u -> u.text(...).param(...))}.
 */
@SuppressWarnings("unused")   // i campi verranno usati nella tua soluzione
@RestController
@Profile("!soluzioni")
@RequestMapping("/ai")
class TraduciEsercizio {

    private final ChatClient chatClient;

    TraduciEsercizio(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    @GetMapping("/traduci")
    String traduci(@RequestParam String testo, @RequestParam String lingua) {
        // TODO: usa chatClient con un template a due segnaposto, {testo} e {lingua},
        //       e chiedi di restituire SOLO la traduzione.
        throw DaSvolgere.esercizio("Parte 1 Facile - /ai/traduci");
    }
}

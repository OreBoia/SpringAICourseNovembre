package it.corso.ai.soluzioni.parte1;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Soluzione Parte 1 — Facile: template con due segnaposto. */
@RestController
@Profile("soluzioni")
@RequestMapping("/ai")
class TraduciSoluzione {

    private final ChatClient chatClient;

    TraduciSoluzione(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    @GetMapping("/traduci")
    String traduci(@RequestParam String testo, @RequestParam String lingua) {
        return chatClient.prompt()
                .user(u -> u.text("Traduci in {lingua} il testo seguente. "
                                + "Restituisci solo la traduzione, senza spiegazioni né virgolette.\n{testo}")
                        .param("lingua", lingua)
                        .param("testo", testo))
                .call()
                .content();
    }
}

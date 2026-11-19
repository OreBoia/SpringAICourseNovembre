package it.corso.ai.soluzioni.parte2;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Soluzione Parte 2 — Facile: estrazione di dati con structured output. */
@RestController
@Profile("soluzioni")
@RequestMapping("/ai")
class LibroSoluzione {

    private final ChatClient chatClient;

    LibroSoluzione(ChatClient.Builder builder) {
        this.chatClient = builder
                .defaultSystem("Estrai titolo, autore e anno di pubblicazione del libro citato nella frase. "
                        + "Usa solo le informazioni presenti nella frase.")
                .build();
    }

    @PostMapping(value = "/libro", consumes = MediaType.TEXT_PLAIN_VALUE)
    Libro libro(@RequestBody String frase) {
        return chatClient.prompt().user(frase).call().entity(Libro.class);
    }
}

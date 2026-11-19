package it.corso.ai.soluzioni.parte1;

import java.util.Map;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Soluzione Parte 1 — Medio: defaultSystem + metadati. */
@RestController
@Profile("soluzioni")
@RequestMapping("/ai")
class CommitSoluzione {

    private final ChatClient chatClient;

    CommitSoluzione(ChatClient.Builder builder) {
        this.chatClient = builder
                .defaultSystem("Sei un revisore di commit message Git. Riscrivi il messaggio ricevuto "
                        + "in inglese, all'imperativo, in una sola riga di massimo 72 caratteri. "
                        + "Restituisci solo la riga, senza virgolette né spiegazioni.")
                .build();
    }

    @GetMapping("/commit")
    Map<String, Object> commit(@RequestParam String messaggio) {
        ChatResponse risposta = chatClient.prompt().user(messaggio).call().chatResponse();
        return Map.of(
                "messaggio", risposta.getResult().getOutput().getText().strip(),
                "tokenTotali", risposta.getMetadata().getUsage().getTotalTokens());
    }
}

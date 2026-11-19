package it.corso.ai.tappa06_memory;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Demo dei concetti teorici (Parte 1, "LLM: stateless"): senza advisor di memoria ogni chiamata
 * parte da zero. "Ciao, sono Anna" e poi "Come mi chiamo?" → il modello non lo sa.
 * Da confrontare con {@code /ai/assistente/{sessione}}.
 */
@RestController
@RequestMapping("/demo")
class SenzaMemoriaController {

    private final ChatClient chatClient;

    SenzaMemoriaController(ChatClient.Builder builder) {
        this.chatClient = builder
                .defaultSystem("Rispondi in italiano, in una riga.")
                .build();   // nessun MessageChatMemoryAdvisor
    }

    @PostMapping("/senza-memoria")
    String chat(@RequestBody String messaggio) {
        return chatClient.prompt().user(messaggio).call().content();
    }
}

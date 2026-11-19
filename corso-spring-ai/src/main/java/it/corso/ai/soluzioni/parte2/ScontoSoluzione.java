package it.corso.ai.soluzioni.parte2;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Soluzione Parte 2 — Medio: un tool di calcolo e un system prompt che ne impone l'uso. */
@RestController
@Profile("soluzioni")
@RequestMapping("/ai")
class ScontoSoluzione {

    private final ChatClient chatClient;
    private final ScontoTools tools = new ScontoTools();

    ScontoSoluzione(ChatClient.Builder builder) {
        this.chatClient = builder
                .defaultSystem("Sei la cassa di un negozio. Per OGNI calcolo di prezzo scontato usa il tool "
                        + "applicaSconto: non fare mai i conti a mente. Rispondi in italiano, in una riga.")
                .build();
    }

    @GetMapping("/sconto")
    String sconto(@RequestParam String domanda) {
        return chatClient.prompt().user(domanda).tools(tools).call().content();
    }
}

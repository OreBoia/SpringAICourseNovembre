package it.corso.ai.tappa03_streaming;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/ai")
class StoriaController {

    private final ChatClient chatClient;

    StoriaController(ChatClient.Builder builder) {
        this.chatClient = builder
                .defaultSystem("Sei un narratore. Scrivi sempre in italiano.")
                .build();
    }

    // text/event-stream: il browser/curl riceve i pezzi appena arrivano
    @GetMapping(value = "/storia", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    Flux<String> storia(@RequestParam String tema) {
        return chatClient.prompt()
                .user(u -> u.text("Racconta in 6 frasi una storia su {tema}")
                        .param("tema", tema))
                .stream()                // risposta a pezzi invece di call()
                .content()               // Flux<String>: un pezzo di testo alla volta
                // un errore a metà flusso non si può più trasformare in HTTP 502:
                // lo comunichiamo come ultimo pezzo
                .onErrorResume(_ -> Flux.just("\n[errore: provider AI non disponibile]"));
    }
}

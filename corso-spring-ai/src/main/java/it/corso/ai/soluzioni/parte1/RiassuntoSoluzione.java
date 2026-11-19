package it.corso.ai.soluzioni.parte1;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Flux;

/** Soluzione Parte 1 — Difficile: streaming SSE con errore leggibile. */
@RestController
@Profile("soluzioni")
@RequestMapping("/ai")
class RiassuntoSoluzione {

    private final ChatClient chatClient;

    RiassuntoSoluzione(ChatClient.Builder builder) {
        this.chatClient = builder
                .defaultSystem("Riassumi il testo che ricevi in italiano, in al massimo 3 punti elenco brevi. "
                        + "Non aggiungere introduzioni né conclusioni.")
                .build();
    }

    @PostMapping(value = "/riassunto", consumes = MediaType.TEXT_PLAIN_VALUE,
            produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    Flux<String> riassunto(@RequestBody String testo) {
        return chatClient.prompt()
                .user(testo)   // testo come messaggio, non come template: eventuali {graffe} non danno problemi
                .stream()
                .content()
                .onErrorResume(_ -> Flux.just("\n[errore: impossibile completare il riassunto, riprova più tardi]"));
    }
}

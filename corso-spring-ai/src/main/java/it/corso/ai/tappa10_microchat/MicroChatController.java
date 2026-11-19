package it.corso.ai.tappa10_microchat;

import java.util.List;

import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import it.corso.ai.tappa10_microchat.MicroChat.Battuta;
import jakarta.servlet.http.HttpSession;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/chat")
class MicroChatController {

    private final MicroChat chat;
    private final Environment env;

    MicroChatController(MicroChat chat, Environment env) {
        this.chat = chat;
        this.env = env;
    }

    // L'ID di conversazione è l'ID della sessione HTTP (cookie JSESSIONID): lo decide il server.
    // Va letto subito, nel thread della richiesta, prima di restituire il Flux.
    @PostMapping(value = "/messaggio", consumes = MediaType.TEXT_PLAIN_VALUE,
            produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    Flux<ServerSentEvent<Object>> messaggio(@RequestBody String messaggio, HttpSession sessione) {
        if (messaggio.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Il messaggio è vuoto");
        }
        return chat.rispondi(sessione.getId(), messaggio.strip());
    }

    @GetMapping("/stato")
    Stato stato(HttpSession sessione) {
        String id = sessione.getId();
        MicroChat.StatoMemoria memoria = chat.stato(id);
        return new Stato(
                id.length() > 8 ? id.substring(0, 8) : id,   // abbreviato: l'ID intero non serve alla pagina
                env.getProperty("spring.ai.openai.chat.model"),
                String.join(",", env.getActiveProfiles()),
                chat.ragAttivo(),
                memoria.messaggiInMemoria(),
                memoria.finestra());
    }

    @GetMapping("/storia")
    List<Battuta> storia(HttpSession sessione) {
        return chat.storia(sessione.getId());
    }

    @PostMapping("/nuova")
    Stato nuova(HttpSession sessione) {
        chat.dimentica(sessione.getId());
        return stato(sessione);
    }

    record Stato(String sessione, String modello, String profili, boolean rag, int messaggiInMemoria, int finestra) {}
}

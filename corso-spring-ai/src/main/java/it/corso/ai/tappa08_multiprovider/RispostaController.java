package it.corso.ai.tappa08_multiprovider;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@Profile("multi")
@RequestMapping("/ai")
class RispostaController {

    private final ClientiAi clienti;

    RispostaController(ClientiAi clienti) {
        this.clienti = clienti;
    }

    @GetMapping("/risposta")
    String risposta(@RequestParam String provider, @RequestParam String domanda) {
        // Scelta del client in base al parametro
        ChatClient client = switch (provider) {
            case "groq" -> clienti.groq();
            case "openai" -> clienti.openai();
            default -> throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Provider sconosciuto: " + provider);
        };
        return client.prompt().user(domanda).call().content();
    }
}

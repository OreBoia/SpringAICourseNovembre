package it.corso.ai.tappa04_structured;

import java.util.List;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ai")
class RicetteController {

    private final ChatClient chatClient;

    RicetteController(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    @GetMapping("/ricetta")
    Ricetta ricetta(@RequestParam String piatto) {
        return chatClient.prompt()
                .user(u -> u.text("Dammi una ricetta semplice di {piatto}")
                        .param("piatto", piatto))
                .call()
                .entity(Ricetta.class);          // al posto di content(): oggetto Java
    }

    @GetMapping("/ricette")
    List<Ricetta> ricette(@RequestParam String ingrediente) {
        return chatClient.prompt()
                .user(u -> u.text("Proponi 3 ricette semplici con {ingrediente}")
                        .param("ingrediente", ingrediente))
                .call()
                // per le liste serve ParameterizedTypeReference (le parentesi {} finali sono volute)
                .entity(new ParameterizedTypeReference<List<Ricetta>>() {});
    }
}

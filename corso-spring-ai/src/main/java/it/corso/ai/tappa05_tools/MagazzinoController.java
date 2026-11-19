package it.corso.ai.tappa05_tools;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ai")
class MagazzinoController {

    private final ChatClient chatClient;
    private final MagazzinoTools tools;

    MagazzinoController(ChatClient.Builder builder, MagazzinoTools tools) {
        this.chatClient = builder
                .defaultSystem("Sei l'assistente del magazzino di un negozio di informatica. "
                        + "Per giacenze e prenotazioni usa SOLO i tool: non inventare numeri. "
                        + "Se un codice non esiste, dillo. Rispondi in italiano, in modo breve.")
                .build();
        this.tools = tools;
    }

    @GetMapping("/magazzino")
    String magazzino(@RequestParam String domanda) {
        return chatClient.prompt()
                .user(domanda)
                .tools(tools)        // mette i tool a disposizione del modello per questa chiamata
                .call()
                .content();
    }
}

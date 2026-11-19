package it.corso.ai.tappa02_tutor;

import java.util.Map;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ai")
class RipassoController {

    private final ChatClient chatClient;

    RipassoController(ChatClient.Builder builder) {
        // Il ruolo vale per tutte le chiamate di questo client
        this.chatClient = builder
                .defaultSystem("Sei un tutor di Java per principianti. "
                        + "Rispondi in italiano, al massimo 5 righe, con un esempio di una riga di codice.")
                .build();
    }

    @GetMapping("/ripasso")
    Map<String, Object> ripasso(@RequestParam String argomento) {
        ChatResponse risposta = chatClient.prompt()
                // {argomento} è un segnaposto del template
                .user(u -> u.text("Spiega {argomento} a uno studente alle prime armi")
                        .param("argomento", argomento))
                .call()
                .chatResponse();   // risposta completa, non solo il testo

        return Map.of(
                "testo", risposta.getResult().getOutput().getText(),
                "tokenTotali", risposta.getMetadata().getUsage().getTotalTokens());
    }
}

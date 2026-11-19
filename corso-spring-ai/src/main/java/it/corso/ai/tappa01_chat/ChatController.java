package it.corso.ai.tappa01_chat;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ai")
class ChatController {

    private final ChatClient chatClient;

    // Spring inietta il Builder auto-configurato dallo starter
    ChatController(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    @GetMapping("/chat")
    String chat(@RequestParam String message) {
        return chatClient.prompt()   // inizia la richiesta
                .user(message)       // messaggio dell'utente
                .call()              // chiamata sincrona al modello
                .content();          // solo il testo della risposta
    }
}

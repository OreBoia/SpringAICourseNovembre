package it.corso.ai.tappa06_memory;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ai")
class AssistenteController {

    private final ChatClient assistente;

    AssistenteController(ChatClient assistente) {   // il bean definito in AssistenteConfig
        this.assistente = assistente;
    }

    @PostMapping("/assistente/{sessione}")
    String chat(@PathVariable String sessione, @RequestBody String messaggio) {
        return assistente.prompt()
                .user(messaggio)
                // senza questo parametro la memoria lancia IllegalArgumentException
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, sessione))
                .call()
                .content();
    }
}

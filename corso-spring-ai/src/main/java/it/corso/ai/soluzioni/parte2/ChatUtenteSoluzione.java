package it.corso.ai.soluzioni.parte2;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Soluzione Parte 2 — Difficile: memoria per utente con finestra di 4 messaggi.
 *
 * <p><b>Perché al passo 5 Anna viene "dimenticata":</b> la finestra conta i messaggi, non gli scambi:
 * ogni domanda e ogni risposta valgono 1. Dopo la presentazione (2 messaggi), la domanda del passo 3 (2)
 * e i tre riempimenti (6), restano solo gli ultimi 4 messaggi: quelli sul mare/luna/vento.
 * Il messaggio "Mi chiamo Anna..." non viene più inviato al modello, che quindi non lo può sapere.
 */
@RestController
@Profile("soluzioni")
@RequestMapping("/ai")
class ChatUtenteSoluzione {

    private final ChatClient chatClient;

    ChatUtenteSoluzione(ChatClient.Builder builder) {
        // Creata qui e non come @Bean: non entra in conflitto con il ChatMemory auto-configurato
        ChatMemory memoriaPiccola = MessageWindowChatMemory.builder().maxMessages(4).build();
        this.chatClient = builder
                .defaultSystem("Rispondi in italiano, in modo breve.")
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(memoriaPiccola).build())
                .build();
    }

    @PostMapping("/chat/{utente}")
    String chat(@PathVariable String utente, @RequestBody String messaggio) {
        return chatClient.prompt()
                .user(messaggio)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, utente))
                .call()
                .content();
    }
}

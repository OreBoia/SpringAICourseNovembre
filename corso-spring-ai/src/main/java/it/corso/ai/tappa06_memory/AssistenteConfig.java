package it.corso.ai.tappa06_memory;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import it.corso.ai.tappa05_tools.MagazzinoTools;

@Configuration
class AssistenteConfig {

    // ChatMemory è auto-configurato (finestra di 20 messaggi, repository in memoria)
    @Bean
    ChatClient assistente(ChatClient.Builder builder, ChatMemory chatMemory, MagazzinoTools tools) {
        return builder
                .defaultSystem("Sei l'assistente del magazzino di un negozio di informatica. "
                        + "Per giacenze e prenotazioni usa SOLO i tool: non inventare numeri. "
                        + "Rispondi in italiano, in modo breve.")
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(chatMemory).build(),  // memoria: registrata a mano
                        new SimpleLoggerAdvisor())                             // log di richiesta e risposta
                .defaultTools(tools)                                           // tool sempre disponibili
                .build();
    }
}

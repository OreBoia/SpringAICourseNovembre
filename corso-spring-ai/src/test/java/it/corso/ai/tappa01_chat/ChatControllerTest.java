package it.corso.ai.tappa01_chat;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;

import it.corso.ai.FakeChatModel;

class ChatControllerTest {

    @Test
    void inoltraIlMessaggioAlModelloERestituisceIlTesto() {
        FakeChatModel modello = FakeChatModel.cheRisponde("Un LLM predice il prossimo token.");
        ChatController controller = new ChatController(ChatClient.builder(modello));

        String risposta = controller.chat("Che cos'è un LLM?");

        assertThat(risposta).isEqualTo("Un LLM predice il prossimo token.");
        assertThat(modello.ultimoPrompt().getUserMessage().getText()).isEqualTo("Che cos'è un LLM?");
    }
}

package it.corso.ai.tappa03_streaming;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;

import it.corso.ai.FakeChatModel;

class StoriaControllerTest {

    @Test
    void laRispostaArrivaAPezzi() {
        StoriaController controller = new StoriaController(
                ChatClient.builder(FakeChatModel.cheRisponde("C'era una volta un robot")));

        List<String> pezzi = controller.storia("robot").collectList().block();

        assertThat(pezzi).containsExactly("C'era ", "una ", "volta ", "un ", "robot");
    }

    @Test
    void unErroreDelProviderDiventaUnUltimoPezzoLeggibile() {
        StoriaController controller = new StoriaController(ChatClient.builder(FakeChatModel.cheFallisce()));

        List<String> pezzi = controller.storia("robot").collectList().block();

        assertThat(pezzi).containsExactly("\n[errore: provider AI non disponibile]");
    }
}

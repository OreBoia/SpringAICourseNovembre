package it.corso.ai.tappa02_tutor;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;

import it.corso.ai.FakeChatModel;

class RipassoControllerTest {

    @Test
    void ilTemplateSostituisceIlSegnapostoEIlSystemPromptArrivaAlModello() {
        FakeChatModel modello = FakeChatModel.cheRisponde("Un'interfaccia è un contratto.");
        RipassoController controller = new RipassoController(ChatClient.builder(modello));

        Map<String, Object> risposta = controller.ripasso("le interfacce");

        Prompt inviato = modello.ultimoPrompt();
        assertThat(inviato.getSystemMessage().getText()).contains("tutor di Java");
        assertThat(inviato.getUserMessage().getText())
                .isEqualTo("Spiega le interfacce a uno studente alle prime armi")   // {argomento} sostituito
                .doesNotContain("{argomento}");
        assertThat(risposta)
                .containsEntry("testo", "Un'interfaccia è un contratto.")
                .containsEntry("tokenTotali", 15);   // i metadati arrivano dalla ChatResponse
    }

    @Test
    void ogniTentativoEUnaChiamataSeparata() {
        int[] contatore = {0};
        FakeChatModel modello = new FakeChatModel(_ -> "risposta " + (++contatore[0]));
        NonDeterminismoController controller = new NonDeterminismoController(ChatClient.builder(modello));

        var tentativi = controller.nonDeterminismo("Inventa un nome", 50);   // limitato a 5

        assertThat(tentativi).hasSize(5);
        assertThat(modello.promptRicevuti()).hasSize(5);
    }
}

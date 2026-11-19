package it.corso.ai.tappa06_memory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;

import it.corso.ai.FakeChatModel;
import it.corso.ai.tappa05_tools.MagazzinoTools;

/**
 * La memoria vista "da dentro": il modello finto risponde con il numero di messaggi che ha ricevuto.
 * Così si vede che l'advisor rimanda al modello la storia della conversazione.
 */
class MemoriaTest {

    private final FakeChatModel modello = new FakeChatModel(p -> "ricevuti " + p.getInstructions().size() + " messaggi");

    private final AssistenteController assistente = new AssistenteController(
            new AssistenteConfig().assistente(ChatClient.builder(modello),
                    MessageWindowChatMemory.builder().build(), new MagazzinoTools()));

    @Test
    void laStessaSessioneRicordaIMessaggiPrecedenti() {
        // system + domanda
        assertThat(assistente.chat("anna", "Ciao, sono Anna")).isEqualTo("ricevuti 2 messaggi");
        // system + (domanda 1 + risposta 1) + domanda 2
        assertThat(assistente.chat("anna", "Come mi chiamo?")).isEqualTo("ricevuti 4 messaggi");
        assertThat(modello.ultimoPrompt().getContents()).contains("Ciao, sono Anna");
    }

    @Test
    void sessioniDiverseHannoMemorieSeparate() {
        assistente.chat("anna", "Ciao, sono Anna");

        assertThat(assistente.chat("luca", "Come mi chiamo?")).isEqualTo("ricevuti 2 messaggi");
        assertThat(modello.ultimoPrompt().getContents()).doesNotContain("Anna");
    }

    @Test
    void senzaMemoriaOgniChiamataParteDaZero() {
        SenzaMemoriaController senza = new SenzaMemoriaController(ChatClient.builder(modello));

        senza.chat("Ciao, sono Anna");

        assertThat(senza.chat("Come mi chiamo?")).isEqualTo("ricevuti 2 messaggi");
    }

    @Test
    void senzaConversationIdLaMemoriaLanciaUnEccezione() {
        ChatClient client = new AssistenteConfig().assistente(ChatClient.builder(modello),
                MessageWindowChatMemory.builder().build(), new MagazzinoTools());

        assertThatThrownBy(() -> client.prompt().user("Ciao").call().content())
                .isInstanceOf(IllegalArgumentException.class);
    }
}

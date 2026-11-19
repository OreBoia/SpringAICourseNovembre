package it.corso.ai.soluzioni.parte1;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;

import it.corso.ai.FakeChatModel;

class SoluzioniParte1Test {

    @Test
    void facile_entrambiISegnapostoVengonoSostituiti() {
        FakeChatModel modello = FakeChatModel.cheRisponde("Good morning everyone");
        TraduciSoluzione controller = new TraduciSoluzione(ChatClient.builder(modello));

        assertThat(controller.traduci("Buongiorno a tutti", "inglese")).isEqualTo("Good morning everyone");
        assertThat(modello.ultimoPrompt().getUserMessage().getText())
                .contains("inglese", "Buongiorno a tutti")
                .doesNotContain("{lingua}", "{testo}");
    }

    @Test
    void medio_restituisceMessaggioETokenTotali() {
        FakeChatModel modello = FakeChatModel.cheRisponde("Fix login failure\n");
        CommitSoluzione controller = new CommitSoluzione(ChatClient.builder(modello));

        assertThat(controller.commit("fix bug login che non funzionava"))
                .containsEntry("messaggio", "Fix login failure")
                .containsEntry("tokenTotali", 15);
        assertThat(modello.ultimoPrompt().getSystemMessage().getText()).contains("72 caratteri");
    }

    @Test
    void difficile_unTestoConGraffeNonRompeIlPrompt_eLErroreELeggibile() {
        RiassuntoSoluzione ok = new RiassuntoSoluzione(ChatClient.builder(FakeChatModel.cheRisponde("- punto uno")));
        assertThat(ok.riassunto("Map<String,{x}> è un tipo generico").collectList().block())
                .containsExactly("- ", "punto ", "uno");

        RiassuntoSoluzione ko = new RiassuntoSoluzione(ChatClient.builder(FakeChatModel.cheFallisce()));
        List<String> pezzi = ko.riassunto("testo").collectList().block();
        assertThat(pezzi.getLast()).startsWith("\n[errore:");
    }
}

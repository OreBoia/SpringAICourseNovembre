package it.corso.ai.soluzioni.parte2;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;

import it.corso.ai.FakeChatModel;

class SoluzioniParte2Test {

    @Test
    void facile_laFraseDiventaUnLibro() {
        LibroSoluzione controller = new LibroSoluzione(ChatClient.builder(FakeChatModel.cheRisponde(
                "{\"titolo\":\"Il nome della rosa\",\"autore\":\"Umberto Eco\",\"anno\":1980}")));

        assertThat(controller.libro("Il nome della rosa è un romanzo di Umberto Eco pubblicato nel 1980."))
                .isEqualTo(new Libro("Il nome della rosa", "Umberto Eco", 1980));
    }

    @Test
    void medio_ilToolCalcolaIPrezziDellEsercizio() {
        ScontoTools tools = new ScontoTools();

        assertThat(tools.applicaSconto(240, 15)).isEqualTo(204.0);
        assertThat(tools.applicaSconto(80, 25)).isEqualTo(60.0);
    }

    @Test
    void difficile_conFinestraDi4AnnaVieneDimenticata() {
        FakeChatModel modello = FakeChatModel.cheRisponde("ok");
        ChatUtenteSoluzione chat = new ChatUtenteSoluzione(ChatClient.builder(modello));

        chat.chat("anna", "Mi chiamo Anna e adoro il basilico");
        chat.chat("luca", "Mi chiamo Luca e adoro la pizza");
        chat.chat("anna", "Come mi chiamo e cosa adoro?");
        assertThat(modello.ultimoPrompt().getContents()).contains("basilico").doesNotContain("pizza");

        chat.chat("anna", "Dimmi una curiosità sul mare");
        chat.chat("anna", "E sulla luna?");
        chat.chat("anna", "E sul vento?");
        chat.chat("anna", "Come mi chiamo e cosa adoro?");

        // Restano solo gli ultimi 4 messaggi della storia: la presentazione non viene più inviata
        assertThat(modello.ultimoPrompt().getContents()).doesNotContain("basilico");
        long storia = modello.ultimoPrompt().getInstructions().stream()
                .map(m -> m.getMessageType())
                .filter(t -> t.getValue().equals("user") || t.getValue().equals("assistant"))
                .count();
        assertThat(storia).isEqualTo(5);   // 4 dalla memoria + la domanda nuova
    }
}

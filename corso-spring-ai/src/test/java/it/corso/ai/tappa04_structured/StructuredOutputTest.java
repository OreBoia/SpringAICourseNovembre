package it.corso.ai.tappa04_structured;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.InstanceOfAssertFactories.MAP;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import it.corso.ai.FakeChatModel;

class StructuredOutputTest {

    @Test
    void ilJsonDelModelloDiventaUnRecord() {
        FakeChatModel modello = FakeChatModel.cheRisponde("""
                {"nome":"Spaghetti aglio e olio","minutiPreparazione":15,"ingredienti":["spaghetti","aglio","olio"]}
                """);
        RicetteController controller = new RicetteController(ChatClient.builder(modello));

        Ricetta ricetta = controller.ricetta("spaghetti aglio olio");

        assertThat(ricetta).isEqualTo(new Ricetta("Spaghetti aglio e olio", 15, List.of("spaghetti", "aglio", "olio")));
        // Spring AI ha aggiunto al prompt le istruzioni di formato (lo schema JSON del record)
        assertThat(modello.ultimoPrompt().getContents()).contains("minutiPreparazione");
    }

    @Test
    void unaListaRichiedeParameterizedTypeReference() {
        FakeChatModel modello = FakeChatModel.cheRisponde("""
                [{"nome":"Zucchine trifolate","minutiPreparazione":20,"ingredienti":["zucchine"]},
                 {"nome":"Frittata di zucchine","minutiPreparazione":25,"ingredienti":["zucchine","uova"]}]
                """);
        RicetteController controller = new RicetteController(ChatClient.builder(modello));

        assertThat(controller.ricette("zucchine")).extracting(r -> r.nome())
                .containsExactly("Zucchine trifolate", "Frittata di zucchine");
    }

    @Test
    void seIlModelloNonRispettaIlFormatoLaConversioneFallisce() {
        RicetteController controller = new RicetteController(
                ChatClient.builder(FakeChatModel.cheRisponde("Certo! Ecco una ricetta buonissima...")));

        assertThatThrownBy(() -> controller.ricetta("pasta")).isInstanceOf(RuntimeException.class);
    }

    @Test
    void leRecensioniVengonoClassificateConUnEnum() {
        FakeChatModel modello = FakeChatModel.cheRisponde("""
                [{"recensione":"Colazione ottima","sentimento":"POSITIVO","temi":["colazione"]}]
                """);
        RecensioniController controller = new RecensioniController(ChatClient.builder(modello));

        ResponseEntity<?> risposta = controller.analizza(List.of("Colazione ottima"));

        assertThat(risposta.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(risposta.getBody()).isEqualTo(List.of(
                new AnalisiRecensione("Colazione ottima", AnalisiRecensione.Sentimento.POSITIVO, List.of("colazione"))));
    }

    @Test
    void unaRispostaMalformataDiventaUn502Chiaro() {
        RecensioniController controller = new RecensioniController(
                ChatClient.builder(FakeChatModel.cheRisponde("[{\"sentimento\":\"ENTUSIASTA\"}]")));   // enum sconosciuto

        ResponseEntity<?> risposta = controller.analizza(List.of("Wow!"));

        assertThat(risposta.getStatusCode()).isEqualTo(HttpStatus.BAD_GATEWAY);
        assertThat(risposta.getBody()).asInstanceOf(MAP).containsKey("errore");
    }
}

package it.corso.ai.tappa05_tools;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/** Un tool è un normale metodo Java: si testa senza nessun modello. */
class MagazzinoToolsTest {

    private final MagazzinoTools tools = new MagazzinoTools();

    @Test
    void giacenzaDiUnCodiceEsistenteEInesistente() {
        assertThat(tools.giacenza("MOUSE-01")).isEqualTo(25);
        assertThat(tools.giacenza("MON-99")).isEqualTo(-1);
    }

    @Test
    void riservaAggiornaLaGiacenza() {
        assertThat(tools.riserva("MON-27", 3)).startsWith("OK");
        assertThat(tools.giacenza("MON-27")).isEqualTo(1);
    }

    @Test
    void gliArgomentiDelModelloVengonoValidati() {
        assertThat(tools.riserva("MON-99", 1)).isEqualTo("ERRORE: codice sconosciuto");
        assertThat(tools.riserva("MON-27", 0)).startsWith("ERRORE");
        assertThat(tools.riserva("MON-27", -5)).startsWith("ERRORE");
        assertThat(tools.riserva("MON-27", 10)).isEqualTo("ERRORE: disponibili solo 4 pezzi");
        assertThat(tools.giacenza("MON-27")).isEqualTo(4);   // nessuna modifica dopo gli errori
    }
}

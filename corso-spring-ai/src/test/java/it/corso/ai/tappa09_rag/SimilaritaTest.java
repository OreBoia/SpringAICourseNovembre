package it.corso.ai.tappa09_rag;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import org.junit.jupiter.api.Test;

/** La similarità del coseno su vettori piccoli, calcolabili a mano alla lavagna. */
class SimilaritaTest {

    @Test
    void vettoriConLaStessaDirezioneValgonoUno() {
        assertThat(SimilaritaController.coseno(new float[] {1, 2}, new float[] {2, 4})).isCloseTo(1.0, within(1e-6));
    }

    @Test
    void vettoriPerpendicolariValgonoZero() {
        assertThat(SimilaritaController.coseno(new float[] {1, 0}, new float[] {0, 1})).isCloseTo(0.0, within(1e-6));
    }

    @Test
    void vettoriOppostiValgonoMenoUno() {
        assertThat(SimilaritaController.coseno(new float[] {1, 1}, new float[] {-1, -1})).isCloseTo(-1.0, within(1e-6));
    }
}

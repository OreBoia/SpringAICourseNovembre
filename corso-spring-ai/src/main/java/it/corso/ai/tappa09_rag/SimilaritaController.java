package it.corso.ai.tappa09_rag;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Demo dei concetti teorici (Parte 3, "Embedding e similarità"): un testo diventa un vettore,
 * e la similarità del coseno misura quanto due significati sono vicini.
 * Il calcolo è scritto a mano apposta: sono tre somme e una divisione.
 */
@RestController
@Profile("rag")
@RequestMapping("/rag")
class SimilaritaController {

    private final EmbeddingModel embeddingModel;

    SimilaritaController(EmbeddingModel embeddingModel) {
        this.embeddingModel = embeddingModel;
    }

    @GetMapping("/similarita")
    Similarita similarita(@RequestParam String a, @RequestParam String b) {
        float[] va = embeddingModel.embed(a);   // testo → vettore
        float[] vb = embeddingModel.embed(b);
        return new Similarita(a, b, coseno(va, vb), va.length, anteprima(va));
    }

    /** Similarità del coseno: prodotto scalare diviso il prodotto delle lunghezze. */
    static double coseno(float[] x, float[] y) {
        double prodotto = 0, normaX = 0, normaY = 0;
        for (int i = 0; i < x.length; i++) {
            prodotto += x[i] * y[i];
            normaX += x[i] * x[i];
            normaY += y[i] * y[i];
        }
        return prodotto / (Math.sqrt(normaX) * Math.sqrt(normaY));
    }

    // I primi numeri del vettore: per far vedere alla classe che "un embedding è una lista di numeri"
    private static float[] anteprima(float[] v) {
        return java.util.Arrays.copyOf(v, Math.min(5, v.length));
    }

    record Similarita(String a, String b, double similarita, int dimensioni, float[] primiValoriDiA) {}
}

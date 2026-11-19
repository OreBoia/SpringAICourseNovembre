package it.corso.ai.tappa04_structured;

import java.util.List;

/**
 * Risultato dell'analisi NLP di una recensione: sentimento (classificazione) e temi (estrazione).
 * L'enum costringe il modello a scegliere tra tre valori: niente "abbastanza positivo" da interpretare a mano.
 */
public record AnalisiRecensione(String recensione, Sentimento sentimento, List<String> temi) {

    public enum Sentimento { POSITIVO, NEUTRO, NEGATIVO }
}

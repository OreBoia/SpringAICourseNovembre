package it.corso.ai.tappa04_structured;

import java.util.List;

// I nomi dei campi guidano il modello: scegliamoli chiari
public record Ricetta(String nome, int minutiPreparazione, List<String> ingredienti) {
}

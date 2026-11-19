package it.corso.ai.soluzioni.parte2;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

/** Tool della soluzione Parte 2 — Medio. Non è un bean: lo crea il controller con {@code new}. */
public class ScontoTools {

    @Tool(description = "Calcola il prezzo finale di un articolo applicando uno sconto percentuale. "
            + "Restituisce il prezzo scontato in euro, arrotondato a due decimali.")
    public double applicaSconto(
            @ToolParam(description = "Prezzo pieno in euro, ad esempio 240") double prezzo,
            @ToolParam(description = "Sconto in percentuale, ad esempio 15 per il 15%") double percentuale) {
        // prezzo - prezzo*percentuale/100 evita errori di arrotondamento come 240*0.85 = 203.99999...
        double scontato = Math.round((prezzo - prezzo * percentuale / 100) * 100) / 100.0;
        System.out.println("[tool] applicaSconto(" + prezzo + ", " + percentuale + ") = " + scontato);
        return scontato;
    }
}

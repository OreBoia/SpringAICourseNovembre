package it.corso.ai.tappa05_tools;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

@Component
public class MagazzinoTools {

    // Magazzino finto, in memoria
    private final Map<String, Integer> giacenze = new ConcurrentHashMap<>(
            Map.of("MOUSE-01", 25, "TAST-02", 0, "MON-27", 4));

    @Tool(description = "Restituisce quanti pezzi sono disponibili di un prodotto, dato il codice. "
            + "Restituisce -1 se il codice non esiste.")
    public int giacenza(@ToolParam(description = "Codice del prodotto, ad esempio MOUSE-01") String codice) {
        System.out.println("[tool] giacenza(" + codice + ")");   // per vedere le chiamate in console
        return giacenze.getOrDefault(codice, -1);
    }

    @Tool(description = "Riserva un numero di pezzi di un prodotto. Restituisce un messaggio con l'esito.")
    public synchronized String riserva(     // synchronized: controllo e scrittura non devono intrecciarsi
            @ToolParam(description = "Codice del prodotto, ad esempio MON-27") String codice,
            @ToolParam(description = "Numero di pezzi da riservare, maggiore di zero") int quantita) {
        System.out.println("[tool] riserva(" + codice + ", " + quantita + ")");
        Integer disponibili = giacenze.get(codice);
        if (disponibili == null) {
            return "ERRORE: codice sconosciuto";
        }
        if (quantita <= 0) {
            return "ERRORE: la quantità deve essere maggiore di zero";
        }
        if (quantita > disponibili) {
            return "ERRORE: disponibili solo " + disponibili + " pezzi";
        }
        giacenze.put(codice, disponibili - quantita);
        return "OK: riservati " + quantita + " pezzi di " + codice + ", ne restano " + (disponibili - quantita);
    }
}

package it.corso.ai.tappa04_structured;

import java.util.List;
import java.util.Map;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Demo dei concetti teorici: un compito NLP classico (analisi del sentimento + estrazione dei temi,
 * slide Parte 1 "Analisi delle recensioni di un hotel") svolto da un LLM con lo structured output.
 * Mostra anche la buona pratica della Parte 2: gestire il fallimento della conversione.
 */
@RestController
@RequestMapping("/demo")
class RecensioniController {

    private final ChatClient chatClient;

    RecensioniController(ChatClient.Builder builder) {
        this.chatClient = builder
                .defaultSystem("Sei un analista di recensioni d'hotel. Per ogni recensione indica il sentimento "
                        + "e da 1 a 3 temi brevi in italiano (es. pulizia, colazione, rumore, personale). "
                        + "Riporta il testo della recensione così com'è, senza modificarlo.")
                .build();
    }

    @PostMapping("/recensioni")
    ResponseEntity<?> analizza(@RequestBody List<String> recensioni) {
        String elenco = String.join("\n", recensioni.stream().map(r -> "- " + r).toList());
        try {
            List<AnalisiRecensione> analisi = chatClient.prompt()
                    .user(u -> u.text("Analizza queste recensioni:\n{elenco}").param("elenco", elenco))
                    .call()
                    .entity(new ParameterizedTypeReference<List<AnalisiRecensione>>() {});
            return ResponseEntity.ok(analisi);
        } catch (RuntimeException e) {
            // Structured output = best effort: JSON malformato o enum sconosciuto → eccezione.
            // Invece di un 500 generico, rispondiamo con un errore chiaro (si potrebbe anche ritentare).
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(Map.of(
                    "errore", "Il modello non ha restituito un JSON valido: riprova o usa un modello più grande",
                    "dettaglio", String.valueOf(e.getMessage())));
        }
    }
}

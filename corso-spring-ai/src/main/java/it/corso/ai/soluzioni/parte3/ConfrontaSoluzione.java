package it.corso.ai.soluzioni.parte3;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import it.corso.ai.tappa08_multiprovider.ClientiAi;

/** Soluzione Parte 3 — Medio: confronto tra provider con tempi e gestione degli errori. */
@RestController
@Profile("multi & soluzioni")
@RequestMapping("/ai")
class ConfrontaSoluzione {

    private final ClientiAi clienti;

    ConfrontaSoluzione(ClientiAi clienti) {
        this.clienti = clienti;
    }

    @GetMapping("/confronta")
    Map<String, Map<String, Object>> confronta(@RequestParam String domanda) {
        Map<String, Map<String, Object>> risultato = new LinkedHashMap<>();
        risultato.put("groq", interroga(clienti.groq(), domanda));
        risultato.put("openai", interroga(clienti.openai(), domanda));
        return risultato;
    }

    // Un provider che fallisce non deve far cadere l'endpoint: l'errore diventa un campo del JSON
    private static Map<String, Object> interroga(ChatClient client, String domanda) {
        Map<String, Object> voce = new LinkedHashMap<>();
        long inizio = System.nanoTime();
        try {
            voce.put("risposta", client.prompt().user(domanda).call().content());
        } catch (RuntimeException e) {
            voce.put("errore", "Provider non disponibile: " + e.getMessage());
        }
        voce.put("durataMs", (System.nanoTime() - inizio) / 1_000_000);
        return voce;
    }
}

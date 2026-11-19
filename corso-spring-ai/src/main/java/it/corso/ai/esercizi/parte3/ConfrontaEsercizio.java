package it.corso.ai.esercizi.parte3;

import java.util.Map;

import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import it.corso.ai.esercizi.DaSvolgere;
import it.corso.ai.tappa08_multiprovider.ClientiAi;

/**
 * <h3>Parte 3 — Esercizio Medio</h3> (attivo con il profilo {@code multi}: {@code -Dspring-boot.run.profiles=groq,multi})
 *
 * <p><b>Obiettivo:</b> creare l'endpoint {@code GET /ai/confronta?domanda=...} che interroga <b>entrambi</b> i provider
 * e confronta risposte e tempi.
 *
 * <p><b>Dati:</b> il record {@link ClientiAi} della tappa 8; per la durata usa un cronometro Java a tua scelta,
 * in millisecondi, attorno a ciascuna chiamata. Domanda di prova: {@code Spiega in una frase cos'è un token}.
 *
 * <p><b>Risultato atteso:</b> un JSON con una voce per provider, ad esempio
 * {@code {"groq":{"risposta":"...","durataMs":412},"openai":{"risposta":"...","durataMs":1630}}}.
 * Se la chiamata a un provider fallisce, quella voce contiene un campo {@code errore} con un messaggio leggibile
 * al posto di {@code risposta}, e l'altro provider risponde comunque; l'endpoint non restituisce mai 500.
 * Per provare il caso d'errore imposta una chiave <b>non valida</b> (es. {@code OPENAI_API_KEY=finta}).
 */
@SuppressWarnings("unused")   // i campi verranno usati nella tua soluzione
@RestController
@Profile("multi & !soluzioni")
@RequestMapping("/ai")
class ConfrontaEsercizio {

    private final ClientiAi clienti;

    ConfrontaEsercizio(ClientiAi clienti) {
        this.clienti = clienti;
    }

    @GetMapping("/confronta")
    Map<String, Object> confronta(@RequestParam String domanda) {
        // TODO: chiama clienti.groq() e clienti.openai(), misura i tempi, cattura gli errori
        throw DaSvolgere.esercizio("Parte 3 Medio - /ai/confronta");
    }
}

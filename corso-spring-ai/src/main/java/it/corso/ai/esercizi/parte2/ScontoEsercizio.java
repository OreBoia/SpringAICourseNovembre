package it.corso.ai.esercizi.parte2;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import it.corso.ai.esercizi.DaSvolgere;

/**
 * <h3>Parte 2 — Esercizio Medio</h3>
 *
 * <p><b>Obiettivo:</b> dare al modello un tool per calcolare i prezzi scontati e verificare che venga davvero chiamato.
 *
 * <p><b>Dati:</b> una classe con il metodo {@code applicaSconto(double prezzo, double percentuale)}, che restituisce
 * il prezzo scontato; un endpoint {@code GET /ai/sconto?domanda=...}. Il system prompt deve imporre di usare il tool
 * per ogni calcolo. Il metodo deve scrivere una riga in console a ogni chiamata.
 *
 * <p><b>Risultato atteso:</b> per {@code Un articolo costa 240 euro con sconto del 15%: quanto si paga?} la risposta
 * indica 204 euro (il tool restituisce {@code 204.0}); per {@code 80 euro con sconto del 25%} indica 60 euro
 * ({@code 60.0}). In console compare una riga per ogni chiamata del tool.
 *
 * <p><b>Ripasso:</b> tappa 5 ({@code MagazzinoTools}, {@code .tools(...)}).
 */
@SuppressWarnings("unused")   // i campi verranno usati nella tua soluzione
@RestController
@Profile("!soluzioni")
@RequestMapping("/ai")
class ScontoEsercizio {

    private final ChatClient chatClient;

    ScontoEsercizio(ChatClient.Builder builder) {
        // TODO: system prompt che impone l'uso del tool per ogni calcolo
        this.chatClient = builder.build();
    }

    @GetMapping("/sconto")
    String sconto(@RequestParam String domanda) {
        // TODO: crea la classe del tool (@Tool, @ToolParam) e passala con .tools(...)
        throw DaSvolgere.esercizio("Parte 2 Medio - /ai/sconto");
    }
}

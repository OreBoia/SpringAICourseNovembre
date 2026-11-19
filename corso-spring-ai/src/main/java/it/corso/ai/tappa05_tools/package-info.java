/**
 * <h2>Tappa 5 — Tools (function calling)</h2>
 *
 * <p><b>Slide:</b> Parte 2 → "Function calling: il ciclo", "Tools in Spring AI: @Tool e @ToolParam",
 * "Tools: sicurezza e limiti dei modelli", esempio <i>Intermedio</i>.
 *
 * <p><b>Concetti:</b>
 * <ul>
 *   <li>il modello <b>chiede</b> di chiamare un metodo; lo esegue sempre l'applicazione;</li>
 *   <li>{@code description} di {@code @Tool}/{@code @ToolParam}: è ciò che il modello legge per decidere;</li>
 *   <li>argomenti = input non fidato → validazione, {@code synchronized}, errori leggibili.</li>
 * </ul>
 *
 * <p><b>Prove</b> (riavviare tra una serie e l'altra: il magazzino è in memoria):
 * <pre>
 * curl "http://localhost:8080/ai/magazzino?domanda=Quanti%20MOUSE-01%20abbiamo%3F"
 * curl "http://localhost:8080/ai/magazzino?domanda=Riserva%203%20pezzi%20di%20MON-27"
 * curl "http://localhost:8080/ai/magazzino?domanda=Quanti%20MON-99%20abbiamo%3F"
 * </pre>
 *
 * <p><b>Cosa osservare:</b> le righe {@code [tool] ...} in console provano che il metodo Java è stato chiamato.
 * Chiedi "che tempo fa?": nessun tool viene chiamato, decide il modello.
 */
package it.corso.ai.tappa05_tools;

/**
 * <h2>Tappa 4 — Structured Output</h2>
 *
 * <p><b>Slide:</b> Parte 2 → "Il problema: da testo a oggetti", "Structured Output con .entity()",
 * "Limiti dello Structured Output", esempio <i>Base</i>. Demo extra legata a Parte 1 → "NLP" (recensioni).
 *
 * <p><b>Concetti:</b>
 * <ul>
 *   <li>{@code .entity(Ricetta.class)} al posto di {@code .content()};</li>
 *   <li>liste con {@code ParameterizedTypeReference<List<...>>};</li>
 *   <li>enum nel record: il modello deve scegliere tra valori fissi;</li>
 *   <li>output "best effort": la conversione può fallire → gestire l'errore.</li>
 * </ul>
 *
 * <p><b>Prove:</b>
 * <pre>
 * curl "http://localhost:8080/ai/ricetta?piatto=spaghetti%20aglio%20olio"
 * curl "http://localhost:8080/ai/ricette?ingrediente=zucchine"
 * curl -X POST -H "Content-Type: application/json" \
 *   -d '["Camera pulitissima, colazione ottima!","Rumore tutta la notte, personale scortese.","Posizione comoda, niente di speciale."]' \
 *   http://localhost:8080/demo/recensioni
 * </pre>
 *
 * <p><b>Cosa osservare:</b> la forma del JSON è sempre quella del record, i valori cambiano. Con un modello
 * piccolo (es. {@code openai/gpt-oss-20b}) la conversione fallisce più spesso: {@code /demo/recensioni}
 * risponde 502 con un messaggio chiaro invece di un 500 generico.
 */
package it.corso.ai.tappa04_structured;

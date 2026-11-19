/**
 * <h2>Tappa 2 — System prompt, template e metadati</h2>
 *
 * <p><b>Slide:</b> Parte 1 → "ChatClient" (default del client, template), "Large Language Model" (token,
 * non determinismo), esempio <i>Intermedio</i>.
 *
 * <p><b>Concetti:</b>
 * <ul>
 *   <li>{@code defaultSystem}: ruolo fisso per tutte le chiamate del client;</li>
 *   <li>template {@code {argomento}} + {@code .param(...)};</li>
 *   <li>{@code chatResponse()}: testo <b>e</b> metadati (token usati → costi).</li>
 * </ul>
 *
 * <p><b>Prove:</b>
 * <pre>
 * curl "http://localhost:8080/ai/ripasso?argomento=le%20interfacce"
 * curl "http://localhost:8080/demo/non-determinismo?domanda=Inventa%20un%20nome%20per%20un%20gatto&amp;n=3"
 * </pre>
 *
 * <p><b>Cosa osservare:</b> la demo {@code /demo/non-determinismo} manda la <i>stessa</i> domanda N volte:
 * le risposte (e i token) cambiano. Collegamento alla teoria: un LLM predice token in modo probabilistico.
 */
package it.corso.ai.tappa02_tutor;

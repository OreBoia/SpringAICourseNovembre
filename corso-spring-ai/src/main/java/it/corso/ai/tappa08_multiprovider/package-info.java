/**
 * <h2>Tappa 8 — Switch del provider a runtime (profilo {@code multi})</h2>
 *
 * <p><b>Slide:</b> Parte 3 → "Strategie di switch: all'avvio o a runtime", esempio <i>Intermedio</i>.
 *
 * <p><b>Attivazione:</b> servono <b>entrambe</b> le chiavi.
 * <pre>
 * export GROQ_API_KEY=... OPENAI_API_KEY=...
 * ./mvnw spring-boot:run -Dspring-boot.run.profiles=groq,multi
 * </pre>
 *
 * <p><b>Concetti:</b>
 * <ul>
 *   <li>{@code OpenAiChatModel.builder().options(...)}: un modello con indirizzo, chiave e modello propri;</li>
 *   <li>{@code ChatClient.create(modello)}: client senza passare dal Builder auto-configurato;</li>
 *   <li>più client in un {@code record}, non in una {@code Map<String, ChatClient>} come bean.</li>
 * </ul>
 *
 * <p><b>Prove:</b>
 * <pre>
 * curl "http://localhost:8080/ai/risposta?provider=groq&amp;domanda=Che%20cos%27%C3%A8%20un%20token%3F"
 * curl "http://localhost:8080/ai/risposta?provider=openai&amp;domanda=Che%20cos%27%C3%A8%20un%20token%3F"
 * curl -i "http://localhost:8080/ai/risposta?provider=gemini&amp;domanda=Ciao"     → 400
 * </pre>
 */
package it.corso.ai.tappa08_multiprovider;

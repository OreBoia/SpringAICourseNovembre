/**
 * <h2>Tappa 1 — Il primo ChatClient</h2>
 *
 * <p><b>Slide:</b> Parte 1 → "Spring AI: che cos'è", "Setup del progetto", "ChatClient", esempio <i>Base</i>.
 *
 * <p><b>Concetti:</b>
 * <ul>
 *   <li>lo starter crea un {@code ChatModel} dalle proprietà {@code spring.ai.openai.*};</li>
 *   <li>Spring inietta il {@code ChatClient.Builder} auto-configurato;</li>
 *   <li>catena minima {@code prompt().user().call().content()}.</li>
 * </ul>
 *
 * <p><b>Prova:</b> {@code curl "http://localhost:8080/ai/chat?message=Spiega%20in%20due%20righe%20cos%27%C3%A8%20un%20LLM"}
 *
 * <p><b>Cosa osservare:</b> chiama due volte con lo stesso messaggio: il testo cambia (modello non deterministico).
 * Il codice non contiene nessun riferimento a Groq o OpenAI: decide la configurazione.
 */
package it.corso.ai.tappa01_chat;

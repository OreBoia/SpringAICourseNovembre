/**
 * <h2>Tappa 6 — Chat Memory e Advisors</h2>
 *
 * <p><b>Slide:</b> Parte 2 → "Chat Memory", "Advisors: concetto, ordine e advisor pronti",
 * "Memory e Advisor insieme", esempio <i>Avanzato</i>. Demo extra legata a Parte 1 → "LLM: stateless".
 *
 * <p><b>Concetti:</b>
 * <ul>
 *   <li>il modello è stateless: {@code /demo/senza-memoria} non ricorda nulla;</li>
 *   <li>repository (dove) + {@code MessageWindowChatMemory} (regola) + {@code MessageChatMemoryAdvisor} (aggancio);</li>
 *   <li>{@code ChatMemory.CONVERSATION_ID} obbligatorio: un ID = una conversazione;</li>
 *   <li>{@code SimpleLoggerAdvisor}: osserva la catena senza cambiare la risposta (log a livello DEBUG).</li>
 * </ul>
 *
 * <p><b>Prove</b> (prima senza memoria, poi con):
 * <pre>
 * curl -X POST -H "Content-Type: text/plain" -d "Ciao, sono Anna" http://localhost:8080/demo/senza-memoria
 * curl -X POST -H "Content-Type: text/plain" -d "Come mi chiamo?"  http://localhost:8080/demo/senza-memoria
 *
 * curl -X POST -H "Content-Type: text/plain" -d "Ciao, sono Anna. Quanti MON-27 abbiamo?" http://localhost:8080/ai/assistente/anna
 * curl -X POST -H "Content-Type: text/plain" -d "Riservane 3 per me" http://localhost:8080/ai/assistente/anna
 * curl -X POST -H "Content-Type: text/plain" -d "Come mi chiamo?" http://localhost:8080/ai/assistente/anna
 * curl -X POST -H "Content-Type: text/plain" -d "Come mi chiamo?" http://localhost:8080/ai/assistente/luca
 * </pre>
 *
 * <p><b>Cosa osservare:</b> nei log DEBUG la storia della conversazione compare nel prompt (una sola volta).
 * Semplificazione didattica: qui l'ID sta nell'URL; in produzione lo decide il server (utente autenticato).
 */
package it.corso.ai.tappa06_memory;

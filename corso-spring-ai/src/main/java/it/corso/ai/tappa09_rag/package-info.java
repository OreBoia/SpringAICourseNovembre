/**
 * <h2>Tappa 9 — RAG (profilo {@code rag})</h2>
 *
 * <p><b>Slide:</b> Parte 3 → "RAG: il problema", "La pipeline RAG", "Embedding e similarità",
 * "Database vettoriali", "Similarity search", "RAG con Spring AI", "Limiti del RAG", esempio <i>Avanzato</i>.
 *
 * <p><b>Attivazione:</b> {@code ./mvnw spring-boot:run -Dspring-boot.run.profiles=groq,rag}
 * (al primo avvio scarica il modello di embedding locale, ~80 MB).
 *
 * <p><b>Percorso consigliato in aula:</b>
 * <ol>
 *   <li><b>Embedding</b> — {@code /rag/similarita}: due frasi diventano due vettori da 384 numeri;
 *       "ferie" e "giorni di vacanza" sono vicini, "ferie" e "password" no;</li>
 *   <li><b>Ricerca</b> — {@code /rag/cerca}: i documenti più simili con il punteggio (nessuna chiamata al modello di chat);</li>
 *   <li><b>RAG completo</b> — {@code /rag/chiedi}: {@code QuestionAnswerAdvisor} cerca, aumenta il prompt, poi genera.</li>
 * </ol>
 *
 * <pre>
 * curl "http://localhost:8080/rag/similarita?a=Quando%20posso%20andare%20in%20ferie%3F&amp;b=Come%20chiedo%20giorni%20di%20vacanza%3F"
 * curl "http://localhost:8080/rag/similarita?a=Quando%20posso%20andare%20in%20ferie%3F&amp;b=La%20password%20scade%20ogni%2090%20giorni"
 * curl "http://localhost:8080/rag/cerca?q=Come%20chiedo%20le%20ferie%3F"
 * curl "http://localhost:8080/rag/chiedi?domanda=Ogni%20quanto%20scade%20la%20password%3F"
 * curl "http://localhost:8080/rag/chiedi?domanda=Chi%20ha%20vinto%20i%20mondiali%20del%202006%3F"
 * </pre>
 *
 * <p><b>Cosa osservare:</b> il modello locale predefinito è addestrato soprattutto su inglese: la domanda sui
 * mondiali ottiene ~0.50, più di alcune domande pertinenti, e supera la soglia 0.4. Riavviare con
 * {@code -Dspring-boot.run.profiles=groq,rag,rag-multilingua} e ripetere le stesse prove: i gruppi si separano
 * (mondiali ~0.04). Numeri completi nel README, sezione "Calibrazione".
 */
package it.corso.ai.tappa09_rag;

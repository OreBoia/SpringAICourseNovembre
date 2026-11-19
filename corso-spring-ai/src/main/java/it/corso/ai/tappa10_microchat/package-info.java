/**
 * <h2>Tappa 10 — Micro chat: tutto il corso in una pagina</h2>
 *
 * <p><b>Slide:</b> tutte e tre le parti. La micro chat mette insieme:
 * <ul>
 *   <li><b>streaming</b> (Parte 1, tappa 3): la risposta compare a pezzi;</li>
 *   <li><b>memoria per sessione</b> (Parte 2, tappa 6): l'ID di conversazione è la sessione HTTP, decisa dal
 *       <b>server</b> e non scelta dal client nell'URL: è la soluzione alla "semplificazione didattica" della tappa 6;</li>
 *   <li><b>tools</b> (Parte 2, tappa 5): giacenze e prenotazioni del magazzino; la pagina mostra ogni chiamata;</li>
 *   <li><b>RAG</b> (Parte 3, tappa 9): con il profilo {@code rag} risponde anche dal regolamento e mostra le fonti;</li>
 *   <li><b>advisor e il loro ordine</b> (Parte 2): memoria → RAG → tools → modello.</li>
 * </ul>
 *
 * <p><b>Avvio</b> (chiave Groq nel file {@code .env}, vedi README):
 * <pre>
 * ./mvnw spring-boot:run                                                         → solo chat, memoria e tools
 * ./mvnw spring-boot:run -Dspring-boot.run.profiles=groq,rag,rag-multilingua     → anche il RAG
 * </pre>
 * Poi aprire <b>http://localhost:8080/chat.html</b>.
 *
 * <p><b>Ordine degli advisor</b> (valori bassi = prima; l'ultimo anello chiama il modello):
 * <ol>
 *   <li>{@code MessageChatMemoryAdvisor} (MIN+200): aggiunge la storia una volta sola e, alla fine,
 *       salva solo la domanda e la risposta finale;</li>
 *   <li>{@code QuestionAnswerAdvisor} (MIN+250, impostato qui): cerca nel regolamento <b>una volta per domanda</b>;
 *       la memoria, che sta prima, salva la domanda originale e non quella gonfiata dal contesto;</li>
 *   <li>{@code ToolCallingAdvisor} (MIN+300, registrato automaticamente): ripete la chiamata al modello finché
 *       il modello chiede tool. I messaggi intermedi dei tool non finiscono in memoria.</li>
 * </ol>
 * Il {@code QuestionAnswerAdvisor} ha ordine 0 di default: starebbe <i>dentro</i> il ciclo dei tool e cercherebbe
 * a ogni giro. Per questo qui l'ordine è impostato a mano.
 *
 * <p><b>Cosa osservare:</b> chiedi una giacenza (compare il "chip" del tool), poi "e ne restano quante?"
 * (memoria), poi "Nuova conversazione" e "come mi chiamo?". Con una chiave sbagliata o finita la quota
 * gratuita, la chat mostra un errore leggibile invece di restare muta.
 */
package it.corso.ai.tappa10_microchat;

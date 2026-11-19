/**
 * <h2>Esercizi della Parte 3</h2>
 *
 * <h3>Facile (solo configurazione, nessuna classe)</h3>
 *
 * <p><b>Obiettivo:</b> aggiungere un terzo profilo, {@code groq-veloce}, che usa Groq con un modello più piccolo e veloce.
 *
 * <p><b>Dati:</b> modello {@code openai/gpt-oss-20b}; stessa chiave e stesso indirizzo del profilo {@code groq}.
 * Con {@code -Dspring-boot.run.profiles=groq-veloce} il profilo {@code groq} <b>non</b> resta attivo: il nuovo file
 * {@code src/main/resources/application-groq-veloce.properties} deve contenere tutte le proprietà, compresa
 * {@code spring.ai.openai.chat.replay-reasoning-content=false}.
 *
 * <p><b>Risultato atteso:</b> avviando con il profilo {@code groq-veloce} (da riga di comando, senza modificare
 * {@code application.properties}), {@code GET /ai/provider} restituisce
 * {@code {"profilo":"groq-veloce","modello":"openai/gpt-oss-20b"}}. Con {@code groq} o {@code openai} i risultati
 * restano quelli della tappa 7. (Soluzione: {@code docs/soluzioni/application-groq-veloce.properties}.)
 *
 * <h3>Medio e Difficile</h3>
 * <p>Vedi {@link it.corso.ai.esercizi.parte3.ConfrontaEsercizio} (profilo {@code multi}) e
 * {@link it.corso.ai.esercizi.parte3.BibliotecaEsercizio} (profilo {@code rag}).
 */
package it.corso.ai.esercizi.parte3;

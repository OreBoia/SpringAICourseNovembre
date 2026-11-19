/**
 * <h2>Soluzioni degli esercizi (profilo {@code soluzioni})</h2>
 *
 * <p>Stesse rotte degli stub in {@code it.corso.ai.esercizi}, attive solo con il profilo {@code soluzioni}:
 * <pre>
 * ./mvnw spring-boot:run -Dspring-boot.run.profiles=groq,soluzioni
 * ./mvnw spring-boot:run -Dspring-boot.run.profiles=groq,multi,soluzioni   (Parte 3 Medio)
 * ./mvnw spring-boot:run -Dspring-boot.run.profiles=groq,rag,soluzioni     (Parte 3 Difficile)
 * </pre>
 * La soluzione della Parte 3 Facile è un file di configurazione: {@code docs/soluzioni/application-groq-veloce.properties}.
 *
 * <p>Le classi di supporto (es. {@code ScontoTools}) <b>non</b> sono bean: vengono create con {@code new},
 * così non entrano in conflitto con classi omonime create dagli studenti.
 */
package it.corso.ai.soluzioni;

/**
 * <h2>Esercizi per gli studenti</h2>
 *
 * <p>Ogni classe ha già l'endpoint mappato e, nel Javadoc, il testo dell'esercizio delle slide
 * (Obiettivo, Dati, Risultato atteso). Il corpo contiene i commenti da completare: finché non lo fai,
 * l'endpoint risponde <b>HTTP 501</b>.
 *
 * <p>Le classi sono attive solo <b>senza</b> il profilo {@code soluzioni} ({@code @Profile("!soluzioni")}).
 * Con {@code -Dspring-boot.run.profiles=groq,soluzioni} vengono sostituite dalle soluzioni del docente
 * (package {@code it.corso.ai.soluzioni}).
 *
 * <p>Consiglio: se crei classi di supporto annotate con {@code @Component} (es. un tool), dagli un nome
 * diverso da quelli delle tappe per evitare conflitti di nomi dei bean.
 */
package it.corso.ai.esercizi;

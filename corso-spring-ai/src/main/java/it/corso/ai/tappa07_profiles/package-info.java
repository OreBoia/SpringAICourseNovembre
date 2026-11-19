/**
 * <h2>Tappa 7 — Spring Boot Profiles: switch del provider all'avvio</h2>
 *
 * <p><b>Slide:</b> Parte 3 → "Perché usare più provider", "Come funziona in Spring AI", "Spring Boot Profiles",
 * esempio <i>Base</i>.
 *
 * <p><b>Concetti:</b>
 * <ul>
 *   <li>{@code application-{profilo}.properties} si aggiunge a {@code application.properties} e vince;</li>
 *   <li>{@code -Dspring-boot.run.profiles=...} ha la precedenza su {@code spring.profiles.active};</li>
 *   <li>il codice Java di tutte le tappe precedenti non cambia: decide la configurazione.</li>
 * </ul>
 *
 * <p><b>Prove:</b>
 * <pre>
 * ./mvnw spring-boot:run                                       → curl http://localhost:8080/ai/provider
 * ./mvnw spring-boot:run -Dspring-boot.run.profiles=openai     → curl http://localhost:8080/ai/provider
 * </pre>
 *
 * <p><b>Cosa osservare:</b> in questo progetto anche {@code rag}, {@code multi} e {@code soluzioni} sono profili:
 * attivano parti del codice con {@code @Profile}. {@code /ai/provider} li elenca tutti.
 */
package it.corso.ai.tappa07_profiles;

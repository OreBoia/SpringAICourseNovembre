/**
 * <h2>Tappa 3 — Streaming (SSE)</h2>
 *
 * <p><b>Slide:</b> Parte 1 → "ChatClient" (quando usare stream), esempio <i>Avanzato</i>.
 *
 * <p><b>Concetti:</b>
 * <ul>
 *   <li>{@code stream()} al posto di {@code call()} → {@code Flux<String>};</li>
 *   <li>{@code produces = TEXT_EVENT_STREAM_VALUE}: Spring MVC invia i pezzi come eventi SSE;</li>
 *   <li>{@code onErrorResume}: lo status HTTP è già 200, l'errore si comunica come ultimo pezzo.</li>
 * </ul>
 *
 * <p><b>Prova:</b> {@code curl -N "http://localhost:8080/ai/storia?tema=un%20robot%20bibliotecario"}
 * (oppure la scheda "Streaming" della pagina web).
 *
 * <p><b>Cosa osservare:</b> riavvia con una chiave sbagliata ({@code GROQ_API_KEY=finta}): ricevi il messaggio
 * di errore leggibile invece di una risposta vuota.
 */
package it.corso.ai.tappa03_streaming;

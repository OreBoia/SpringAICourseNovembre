package it.corso.ai.tappa10_microchat;

import java.util.Locale;

/**
 * Traduce le eccezioni del provider in messaggi comprensibili per chi usa la chat.
 *
 * <p>In aula gli errori più comuni con Groq sono: chiave assente o sbagliata (401), quota gratuita finita (429),
 * modello ritirato, chiamata di tool malformata generata dal modello. Senza questa traduzione la chat
 * resterebbe muta o mostrerebbe uno stack trace.
 */
final class MessaggiErrore {

    private MessaggiErrore() {
    }

    static String spiega(Throwable errore) {
        String tutto = descrivi(errore).toLowerCase(Locale.ROOT);

        if (tutto.contains("401") || tutto.contains("unauthorized") || tutto.contains("invalid api key")
                || tutto.contains("invalid_api_key")) {
            return "Chiave Groq mancante o non valida: controlla GROQ_API_KEY nel file .env e riavvia l'applicazione.";
        }
        if (tutto.contains("429") || tutto.contains("rate limit") || tutto.contains("ratelimit")
                || tutto.contains("rate_limit")) {
            return "Limite del piano gratuito di Groq raggiunto: aspetta qualche secondo e riprova.";
        }
        if (tutto.contains("tool_use_failed") || tutto.contains("failed to call a function")) {
            return "Il modello ha generato una chiamata di tool non valida: riprova o riformula la domanda.";
        }
        if (tutto.contains("model") && (tutto.contains("not found") || tutto.contains("does not exist")
                || tutto.contains("decommissioned") || tutto.contains("model_not_found"))) {
            return "Il modello configurato non è disponibile su Groq: controlla spring.ai.openai.chat.model.";
        }
        if (tutto.contains("unknownhost") || tutto.contains("connect") || tutto.contains("timeout")
                || tutto.contains("timed out")) {
            return "Provider AI non raggiungibile: controlla la connessione di rete.";
        }
        String messaggio = errore.getMessage() == null ? errore.getClass().getSimpleName() : errore.getMessage();
        return "Errore del provider AI: " + (messaggio.length() > 200 ? messaggio.substring(0, 200) + "…" : messaggio);
    }

    // Nome della classe e messaggio di tutta la catena delle cause: l'errore HTTP vero è spesso in fondo
    private static String descrivi(Throwable errore) {
        StringBuilder sb = new StringBuilder();
        for (Throwable t = errore; t != null && sb.length() < 4000; t = t.getCause()) {
            sb.append(t.getClass().getName()).append(": ").append(t.getMessage()).append('\n');
            if (t.getCause() == t) {
                break;
            }
        }
        return sb.toString();
    }
}

package it.corso.ai.esercizi;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/**
 * Segnaposto per gli esercizi: finché non lo sostituisci con la tua soluzione,
 * l'endpoint risponde HTTP 501 (Not Implemented) con un messaggio leggibile.
 */
public final class DaSvolgere {

    private DaSvolgere() {
    }

    public static ResponseStatusException esercizio(String nome) {
        return new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED,
                "Esercizio da svolgere: " + nome + " (le soluzioni si vedono con il profilo 'soluzioni')");
    }
}

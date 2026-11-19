package it.corso.ai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Punto di avvio del progetto didattico.
 *
 * <p>Il progetto è diviso in <b>tappe</b>, una per package ({@code tappa01_chat} ... {@code tappa09_rag}),
 * nello stesso ordine delle slide. Le tappe 1-7 sono sempre attive; la tappa 8 si accende con il
 * profilo {@code multi}, la tappa 9 con il profilo {@code rag}. Vedi il README.
 */
@SpringBootApplication
public class CorsoAiApplication {

    public static void main(String[] args) {
        SpringApplication.run(CorsoAiApplication.class, args);
    }
}

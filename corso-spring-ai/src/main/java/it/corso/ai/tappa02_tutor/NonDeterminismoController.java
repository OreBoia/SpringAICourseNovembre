package it.corso.ai.tappa02_tutor;

import java.util.ArrayList;
import java.util.List;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Demo dei concetti teorici (Parte 1, "Large Language Model"):
 * la stessa domanda inviata più volte produce risposte diverse, e ogni chiamata ha un costo in token.
 */
@RestController
@RequestMapping("/demo")
class NonDeterminismoController {

    private static final int MAX_TENTATIVI = 5;   // ogni tentativo è una chiamata a pagamento (o a quota)

    private final ChatClient chatClient;

    NonDeterminismoController(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    @GetMapping("/non-determinismo")
    List<Tentativo> nonDeterminismo(@RequestParam String domanda,
                                    @RequestParam(defaultValue = "3") int n) {
        int volte = Math.clamp(n, 1, MAX_TENTATIVI);
        List<Tentativo> tentativi = new ArrayList<>();
        for (int i = 1; i <= volte; i++) {
            ChatResponse r = chatClient.prompt().user(domanda).call().chatResponse();
            tentativi.add(new Tentativo(i,
                    r.getResult().getOutput().getText(),
                    r.getMetadata().getUsage().getTotalTokens()));
        }
        return tentativi;
    }

    record Tentativo(int numero, String risposta, Integer token) {}
}

package it.corso.ai.esercizi.parte2;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import it.corso.ai.esercizi.DaSvolgere;

/**
 * <h3>Parte 2 — Esercizio Difficile</h3>
 *
 * <p><b>Obiettivo:</b> realizzare una chat con memoria separata per utente e osservare cosa succede quando
 * la finestra è troppo piccola.
 *
 * <p><b>Dati:</b> una {@code ChatMemory} con finestra di 4 messaggi
 * ({@code MessageWindowChatMemory.builder().maxMessages(4).build()}), creata <b>nel costruttore</b> di questo
 * controller e passata a {@code MessageChatMemoryAdvisor.builder(...)}; non definire un secondo bean
 * {@code ChatMemory}. Un endpoint {@code POST /ai/chat/{utente}} che invia al modello il testo del body;
 * l'ID di conversazione è l'utente nel path. Scenario di prova, in ordine:
 * <ol>
 *   <li>{@code anna}: {@code Mi chiamo Anna e adoro il basilico}</li>
 *   <li>{@code luca}: {@code Mi chiamo Luca e adoro la pizza}</li>
 *   <li>{@code anna} e poi {@code luca}: {@code Come mi chiamo e cosa adoro?}</li>
 *   <li>tre messaggi di riempimento a {@code anna} ({@code Dimmi una curiosità sul mare}, {@code E sulla luna?}, {@code E sul vento?})</li>
 *   <li>{@code anna}: {@code Come mi chiamo e cosa adoro?}</li>
 * </ol>
 *
 * <p><b>Risultato atteso:</b> al passo 3 ciascuno riceve i propri dati, senza scambi. Al passo 5 il modello in genere
 * <b>non</b> sa più rispondere a {@code anna}. Scrivi in 2-3 righe perché (finestra, messaggi conteggiati).
 *
 * <p><b>Ripasso:</b> tappa 6 ({@code AssistenteConfig}, {@code AssistenteController}).
 */
@SuppressWarnings("unused")   // i campi verranno usati nella tua soluzione
@RestController
@Profile("!soluzioni")
@RequestMapping("/ai")
class ChatUtenteEsercizio {

    private final ChatClient chatClient;

    ChatUtenteEsercizio(ChatClient.Builder builder) {
        // TODO: crea la ChatMemory con finestra 4 e registra il MessageChatMemoryAdvisor
        this.chatClient = builder.build();
    }

    @PostMapping("/chat/{utente}")
    String chat(@PathVariable String utente, @RequestBody String messaggio) {
        // TODO: passa l'utente come ChatMemory.CONVERSATION_ID
        throw DaSvolgere.esercizio("Parte 2 Difficile - /ai/chat/{utente}");
    }
}

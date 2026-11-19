# Spring AI — Parte 2: Structured Output, Tools, Chat Memory e Advisors

## Spiegazione teorica

### Il problema: da testo a oggetti

- Un LLM restituisce **testo libero**. Un'applicazione Java lavora con **oggetti**, numeri, date, liste.
- Estrarre i dati da una frase scritta dal modello (con `split`, espressioni regolari...) è fragile: basta una parola diversa e il codice si rompe.
- Con la Parte 1 il modello **parla soltanto**. Tre strumenti lo rendono utilizzabile dentro un'applicazione:
  - **Structured Output:** la risposta diventa un oggetto Java.
  - **Tools:** il modello può chiedere all'applicazione di eseguire codice (leggere un dato, fare un calcolo).
  - **Chat Memory:** la conversazione ha una memoria.
- Un quarto concetto tiene insieme tutto: gli **Advisors**, i pezzi che si agganciano alla chiamata.

### Structured Output con `.entity()`

- **Idea:** al posto di `.content()` (testo) si chiama `.entity(Tipo.class)` e si ottiene direttamente un oggetto.
  - `chatClient.prompt().user(...).call().entity(Ricetta.class)`
- **Come funziona:**
  - Spring AI aggiunge al prompt le **istruzioni di formato** (in base alla struttura del tipo).
  - Il modello risponde in JSON.
  - Spring AI converte il JSON nell'oggetto.
- **Tipi consigliati:** i `record` Java. Sono compatti e i nomi dei campi (`nome`, `minutiPreparazione`) guidano il modello: scegli nomi chiari.
- **Liste e tipi generici:** `.entity(new ParameterizedTypeReference<List<Ricetta>>() {})` (`org.springframework.core.ParameterizedTypeReference`).
- **Nel controller:** restituire il record basta. Spring MVC lo serializza in JSON (Jackson 3 in Spring Boot 4) senza configurazione.

### Limiti dello Structured Output

- L'output è **best effort**: il modello *dovrebbe* seguire il formato, ma non è garantito.
- La conversione può **fallire** (JSON malformato, campo mancante, tipo sbagliato): l'applicazione riceve un'eccezione.
- `.entity()` funziona solo con `call()`, **non** con `stream()`: l'oggetto si costruisce a risposta completa.
- Il formato corretto non garantisce **contenuto corretto**: il modello può inventare un valore che nel testo non c'era. Valida i dati (es. minuti negativi) e nel prompt dì cosa fare se un'informazione manca.
- Modelli piccoli rispettano il formato peggio di quelli grandi.
- Buona pratica: gestire l'errore (messaggio chiaro, nuovo tentativo, risposta di ripiego) invece di far cadere l'endpoint.

### Function calling: il ciclo

- **Problema:** il modello non conosce i tuoi dati in tempo reale (giacenze, saldi, orari) e sbaglia i calcoli esatti.
- **Function calling (tool calling):** il modello può **chiedere** all'applicazione di eseguire una funzione e poi usare il risultato.
- **Il ciclo, passo per passo:**
  1. L'applicazione invia il prompt **più l'elenco dei tool** (nome, descrizione, parametri).
  2. Il modello decide: risponde subito oppure chiede di chiamare un tool con certi argomenti.
  3. L'applicazione **esegue** il metodo Java.
  4. Il risultato torna al modello.
  5. Il modello scrive la risposta finale (anche dopo più chiamate di tool).
- **Confusione tipica:** il modello **non esegue codice**. Lo esegue sempre la tua applicazione.
- In Spring AI 2.0 il ciclo è gestito nella catena degli advisor (`ToolCallingAdvisor`, registrato automaticamente dal `ChatClient`): non scrivi tu il ciclo.

### Tools in Spring AI: `@Tool` e `@ToolParam`

- Un tool è un **metodo Java** annotato:
  - `@Tool(description = "...")` (`org.springframework.ai.tool.annotation.Tool`): dice al modello **cosa fa** il metodo.
  - `@ToolParam(description = "...", required = ...)`: descrive ogni parametro.
- **La descrizione è fondamentale:** il modello decide se e quando chiamare il tool leggendola. Una descrizione vaga produce chiamate sbagliate o assenti.
- Spring AI genera da solo lo **schema JSON** dei parametri a partire dalla firma del metodo. Un parametro `@Nullable` diventa opzionale.
- **Come si passano al client:**
  - `.tools(oggetto)` su una singola chiamata;
  - `.defaultTools(oggetto)` sul builder, per tutte le chiamate.
- In 2.0 i bean `Function` risolti per nome non sono più supportati: i tool si passano sempre in modo esplicito.

### Tools: sicurezza e limiti dei modelli

- **Il modello decide** se e quando chiamare un tool, e con quali argomenti. Gli argomenti sono **input non fidato**: validali sempre (codice sconosciuto, quantità negativa).
- **Effetti collaterali:** un tool che modifica dati (riserva, cancella, paga) può essere chiamato più volte o con valori sbagliati. Da pensare prima:
  - chiedere conferma all'utente per le azioni importanti;
  - esporre il minimo indispensabile (mai "esegui una query SQL qualsiasi");
  - rendere sicuro l'accesso concorrente (es. `synchronized`).
- **Errori leggibili:** restituisci valori o messaggi che il modello sa interpretare (es. `-1` per "non trovato"), non eccezioni misteriose.
- **Costo e latenza:** ogni chiamata di tool aggiunge un giro di richiesta al modello.
- **Non tutti i modelli supportano i tool.** Spring AI li usa solo se il modello li supporta. Prima della lezione verifica nella documentazione Groq che il modello scelto supporti i tool: i modelli vengono ritirati e cambiano nel tempo. Sintomo di un modello senza tool: risponde a memoria, senza righe `[tool]` in console, oppure la chiamata fallisce. Alternativa: `gpt-5-mini`.
- Non basta dire al modello "non inventare": nel system prompt va chiesto di usare i tool per i numeri e di dire quando un dato non esiste.

### Chat Memory

- Ricordiamo dalla Parte 1: il modello è **stateless**. Se scrivi "Mi chiamo Anna" e poi "Come mi chiamo?", la seconda richiesta non contiene più la prima.
- **Chat memory:** l'applicazione conserva i messaggi precedenti e li **rimanda al modello** a ogni richiesta.
- In Spring AI il `ChatMemory` è auto-configurato:
  - `MessageWindowChatMemory`: tiene **al massimo** N messaggi (**finestra**, predefinita 20), tagliando i più vecchi a turni interi. Domanda e risposta contano 2 messaggi (20 sono circa 10 scambi); i messaggi di sistema restano;
  - `InMemoryChatMemoryRepository`: li salva in memoria (si perdono al riavvio).
- Finestra personalizzata: `MessageWindowChatMemory.builder().maxMessages(n).build()`, esposta come bean `ChatMemory`.
- **Memoria non è cronologia:** la finestra **taglia** i messaggi più vecchi, che il modello non vede più.
- Più messaggi inviati = più token = più costo (e rischio di superare la finestra di contesto).
- Per salvare le conversazioni in modo permanente si usano repository persistenti (JDBC e altri) tramite starter dedicati.

### Advisors: concetto, ordine e advisor pronti

- **Advisor** = intercettore della catena di chiamata del `ChatClient`, come un filtro HTTP o `@Transactional`: si inserisce prima e dopo l'operazione senza modificarla.
- **Cosa fa:** riceve la richiesta (`ChatClientRequest`), può modificarla, passa la mano all'anello successivo e poi può modificare la risposta (`ChatClientResponse`). Tra advisor c'è un **contesto condiviso**.
- **Ordine:** ogni advisor ha `getOrder()`; i valori **bassi** vanno per primi. L'ultimo anello chiama il modello.
- **Interfacce** nel pacchetto `org.springframework.ai.chat.client.advisor.api`.
- **Advisor già pronti:**
  - `MessageChatMemoryAdvisor`: memoria della conversazione;
  - `SimpleLoggerAdvisor`: scrive richiesta e risposta nei log (livello `DEBUG`);
  - `QuestionAnswerAdvisor`: RAG (Parte 3);
  - `ToolCallingAdvisor`: il ciclo dei tool, già presente di default.
- **Registrazione:** `defaultAdvisors(...)` sul builder (per tutte le chiamate) oppure `.advisors(...)` sulla singola chiamata.

### Memory e Advisor insieme: conversationId, finestra, persistenza

- **La memoria è fatta di tre pezzi distinti:**
  - `InMemoryChatMemoryRepository` (**dove**): conserva i messaggi, in RAM (con un repository JDBC, su database).
  - `MessageWindowChatMemory` (**la regola**): decide quanti messaggi tenere (la finestra). È un oggetto passivo: da solo non fa nulla.
  - `MessageChatMemoryAdvisor` (**l'aggancio**): prima della chiamata legge la storia dal `ChatMemory` e la aggiunge al prompt; dopo la risposta salva i nuovi messaggi.
- **Flusso:** richiesta → advisor (legge la storia) → modello → advisor (salva) → risposta.
- Il bean `ChatMemory` auto-configurato **non cambia nulla** finché non registri l'advisor.
- **Perché lo registri a mano:** la reference mostra solo la registrazione manuale e non dice se il `ChatClient.Builder` auto-configurato lo aggiunga già. Con `SimpleLoggerAdvisor` controlla nei log che la storia compaia una sola volta nel prompt.
- **Collegamento:** si registra `MessageChatMemoryAdvisor.builder(chatMemory).build()` con `defaultAdvisors(...)`. Registralo sempre a mano.
- **Conversation ID obbligatorio:** a ogni chiamata si indica di quale conversazione si tratta:
  - `.advisors(a -> a.param(ChatMemory.CONVERSATION_ID, id))`
- **Se manca** l'ID: `IllegalArgumentException`. Non esiste un ID di default.
- **Un ID = una conversazione.** ID diversi → memorie separate (un utente, una sessione). Lo stesso ID usato da due persone fa condividere loro la conversazione.
- **Dove prendere l'ID:** dall'identità dell'utente o da un identificatore di sessione deciso dal server, non da un valore indovinabile.
- **Finestra:** oltre il limite i messaggi più vecchi vengono dimenticati.
- **Persistenza:** con il repository in memoria un riavvio azzera tutto.
- `PromptChatMemoryAdvisor` è stato rimosso: nelle guide vecchie lo troverai, non usarlo.

## Esempi teorici di utilizzo

- **Estrarre i dati da un'email d'ordine (structured output).** Un negozio riceve email scritte in modo libero ("vorrei 3 mouse per venerdì, spedire a Torino"). Serve un oggetto con quantità, prodotto e città da salvare nel database. Con `.entity()` il modello compila un record e il codice lavora su campi tipizzati, senza analizzare il testo a mano.
- **Classificare i ticket di assistenza (lista di oggetti).** L'help desk riceve decine di segnalazioni al giorno. Il modello restituisce una lista di record (categoria, urgenza, riassunto) con `ParameterizedTypeReference<List<...>>`. L'applicazione ordina e smista i ticket senza lettura manuale.
- **Quando NON usare lo structured output.** In una chat in cui la risposta è letta da una persona, un oggetto non serve: basta il testo. Non va usato nemmeno con lo `stream()`, perché `.entity()` lavora a risposta completa. Per dati critici (importi, scadenze) i valori estratti vanno comunque validati.
- **Assistente di magazzino con un tool (function calling).** Un negozio informatico vuole rispondere a "quanti monitor abbiamo?". Il modello non conosce le giacenze e rischia di inventare un numero. Con un tool che legge la giacenza reale, il modello chiede il dato all'applicazione e riporta quello esatto.
- **Tool che modifica dati (sicurezza).** Lo stesso assistente può anche riservare pezzi. Qui nasce il rischio: il modello potrebbe chiamare il tool due volte o con una quantità sbagliata. Il tool deve validare gli argomenti, essere sicuro in caso di accesso concorrente e, per azioni importanti, far confermare l'utente.
- **Chatbot di assistenza che ricorda (chat memory).** Un cliente scrive "il mio ordine è in ritardo", poi "e il rimborso?". Senza memoria il modello non sa di quale ordine si parli. Con la memoria, la seconda domanda arriva con il contesto della prima.
- **Una chat per ogni utente (conversation ID).** Un portale interno ha centinaia di utenti connessi. Ogni utente usa un proprio ID di conversazione, così le memorie restano separate. Se due persone usassero lo stesso ID vedrebbero (e influenzerebbero) la stessa conversazione: un problema di privacy.
- **Controllare cosa viene inviato al modello (advisor).** Un team vuole vedere nei log ogni richiesta e risposta per capire perché il modello sbaglia e quanto costa. Aggiunge `SimpleLoggerAdvisor` senza toccare il codice dei controller: la logica trasversale sta nella catena, non nei metodi.

## Esempi di codice

> Gli esempi di questo file **non sono stati eseguiti** (nell'ambiente di lavoro non era possibile scaricare le dipendenze Maven né chiamare il provider). Import, firme e versioni sono stati verificati sulla reference Spring AI 2.0, tranne i punti indicati come **da verificare** nelle righe di contesto. Prima della lezione: provarli con una chiave valida. **Da verificare** anche che un bean `ChatMemory` definito dal corso (esercizio Difficile) sostituisca quello auto-configurato senza conflitti all'avvio. Tutti gli esempi dipendono dal progetto della Parte 1 (stesso `pom.xml`, stesse proprietà, pacchetto `it.corso.ai`).

### Base

Structured Output: un record `Ricetta` e due endpoint, uno che restituisce un oggetto e uno che restituisce una lista. Dipende dal progetto della Parte 1 (nessuna nuova dipendenza).

`Ricetta.java`

```java
package it.corso.ai;

import java.util.List;

// I nomi dei campi guidano il modello: scegliamoli chiari
public record Ricetta(String nome, int minutiPreparazione, List<String> ingredienti) {
}
```

`RicetteController.java`

```java
package it.corso.ai;

import java.util.List;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ai")
class RicetteController {

    private final ChatClient chatClient;

    RicetteController(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    @GetMapping("/ricetta")
    Ricetta ricetta(@RequestParam String piatto) {
        return chatClient.prompt()
                .user(u -> u.text("Dammi una ricetta semplice di {piatto}")
                        .param("piatto", piatto))
                .call()
                .entity(Ricetta.class);          // al posto di content(): oggetto Java
    }

    @GetMapping("/ricette")
    List<Ricetta> ricette(@RequestParam String ingrediente) {
        return chatClient.prompt()
                .user(u -> u.text("Proponi 3 ricette semplici con {ingrediente}")
                        .param("ingrediente", ingrediente))
                .call()
                // per le liste serve ParameterizedTypeReference (le parentesi {} finali sono volute)
                .entity(new ParameterizedTypeReference<List<Ricetta>>() {});
    }
}
```

Prova:

```bash
curl "http://localhost:8080/ai/ricetta?piatto=spaghetti%20aglio%20olio"
curl "http://localhost:8080/ai/ricette?ingrediente=zucchine"
```

Output atteso (i valori variano a ogni chiamata):

```json
{"nome":"Spaghetti aglio, olio e peperoncino","minutiPreparazione":15,"ingredienti":["spaghetti","aglio","olio extra vergine","peperoncino","sale"]}
```

```json
[{"nome":"Zucchine trifolate","minutiPreparazione":20,"ingredienti":["zucchine","aglio","olio","prezzemolo"]}, {"nome":"Frittata di zucchine","minutiPreparazione":25,"ingredienti":["zucchine","uova","parmigiano"]}, {"nome":"Pasta con le zucchine","minutiPreparazione":20,"ingredienti":["pasta","zucchine","pecorino"]}]
```

- `.entity(Ricetta.class)` sostituisce `.content()`: Spring AI aggiunge al prompt le istruzioni di formato e converte il JSON ricevuto nel record.
- Il controller restituisce il record e Spring MVC lo trasforma in JSON: nessuna configurazione.
- Per `List<Ricetta>` la classe da sola non basta (il tipo generico si perde a runtime): serve `ParameterizedTypeReference`.
- I valori sono **generati dal modello**: la forma è quella del record, il contenuto cambia a ogni chiamata.
- Se il modello non rispetta il formato, la conversione solleva un'eccezione e l'endpoint risponde con errore 500.

### Intermedio

Tools: un assistente di magazzino che legge le giacenze e riserva pezzi tramite metodi Java. Dipende dal progetto della Parte 1. Con Groq, usa un modello che supporta i tool (**da verificare** sulla console Groq) oppure `gpt-5-mini`.

`MagazzinoTools.java`

```java
package it.corso.ai;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

@Component
public class MagazzinoTools {

    // Magazzino finto, in memoria
    private final Map<String, Integer> giacenze = new ConcurrentHashMap<>(
            Map.of("MOUSE-01", 25, "TAST-02", 0, "MON-27", 4));

    @Tool(description = "Restituisce quanti pezzi sono disponibili di un prodotto, dato il codice. "
            + "Restituisce -1 se il codice non esiste.")
    public int giacenza(@ToolParam(description = "Codice del prodotto, ad esempio MOUSE-01") String codice) {
        System.out.println("[tool] giacenza(" + codice + ")");   // per vedere le chiamate in console
        return giacenze.getOrDefault(codice, -1);
    }

    @Tool(description = "Riserva un numero di pezzi di un prodotto. Restituisce un messaggio con l'esito.")
    public synchronized String riserva(     // synchronized: controllo e scrittura non devono intrecciarsi
            @ToolParam(description = "Codice del prodotto, ad esempio MON-27") String codice,
            @ToolParam(description = "Numero di pezzi da riservare, maggiore di zero") int quantita) {
        System.out.println("[tool] riserva(" + codice + ", " + quantita + ")");
        Integer disponibili = giacenze.get(codice);
        if (disponibili == null) {
            return "ERRORE: codice sconosciuto";
        }
        if (quantita <= 0) {
            return "ERRORE: la quantità deve essere maggiore di zero";
        }
        if (quantita > disponibili) {
            return "ERRORE: disponibili solo " + disponibili + " pezzi";
        }
        giacenze.put(codice, disponibili - quantita);
        return "OK: riservati " + quantita + " pezzi di " + codice + ", ne restano " + (disponibili - quantita);
    }
}
```

`MagazzinoController.java`

```java
package it.corso.ai;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ai")
class MagazzinoController {

    private final ChatClient chatClient;
    private final MagazzinoTools tools;

    MagazzinoController(ChatClient.Builder builder, MagazzinoTools tools) {
        this.chatClient = builder
                .defaultSystem("Sei l'assistente del magazzino di un negozio di informatica. "
                        + "Per giacenze e prenotazioni usa SOLO i tool: non inventare numeri. "
                        + "Se un codice non esiste, dillo. Rispondi in italiano, in modo breve.")
                .build();
        this.tools = tools;
    }

    @GetMapping("/magazzino")
    String magazzino(@RequestParam String domanda) {
        return chatClient.prompt()
                .user(domanda)
                .tools(tools)        // mette i tool a disposizione del modello per questa chiamata
                .call()
                .content();
    }
}
```

Prova:

```bash
curl "http://localhost:8080/ai/magazzino?domanda=Quanti%20MOUSE-01%20abbiamo%3F"
curl "http://localhost:8080/ai/magazzino?domanda=Riserva%203%20pezzi%20di%20MON-27"
curl "http://localhost:8080/ai/magazzino?domanda=Quanti%20MON-99%20abbiamo%3F"
```

Output atteso (il testo varia, i numeri no):

```
Di MOUSE-01 ne abbiamo 25 pezzi disponibili.
Ho riservato 3 pezzi di MON-27: ne restano 1.
Il codice MON-99 non esiste in magazzino.
```

In console compaiono righe come `[tool] giacenza(MOUSE-01)`: provano che il metodo Java è stato davvero chiamato.

I numeri valgono alla **prima** esecuzione: il magazzino è in memoria, quindi dopo la riserva MON-27 ha 1 pezzo e una seconda prova di «Riserva 3 pezzi di MON-27» risponde che ne resta solo 1. Riavvia l'applicazione tra le prove.

- Il metodo con `@Tool` è il tool; `description` e `@ToolParam` sono ciò che il modello legge per decidere come usarlo.
- Spring AI genera lo schema JSON dei parametri dalla firma (`String codice`, `int quantita`).
- `.tools(tools)` registra i metodi per questa chiamata. Il ciclo (richiesta del modello → esecuzione → risultato → risposta) lo gestisce il `ChatClient`.
- `giacenza` restituisce `-1` per un codice sconosciuto: il system prompt dice al modello di comunicarlo e non di inventare.
- `riserva` valida gli argomenti e restituisce un messaggio di esito leggibile dal modello.
- **Cosa succede se** togli il system prompt? Il modello può rispondere a memoria senza chiamare il tool, con numeri inventati: nelle righe di console non compare nessun `[tool]`.
- **Cosa succede se** il modello scelto non supporta i tool (possibile su Groq)? La chiamata fallisce o il modello risponde senza usarli. Prova con un modello che li supporta o con OpenAI.
- **Cosa succede se** la domanda non riguarda il magazzino ("che tempo fa?")? Il modello non chiama nessun tool: decide lui, non il codice.

### Avanzato

Chat con memoria per sessione, con i tool del magazzino e il log delle chiamate. Dipende dal progetto della Parte 1 e dalla classe `MagazzinoTools` dell'esempio Intermedio. **Da verificare** con l'IDE: il pacchetto di `ChatMemory` (`org.springframework.ai.chat.memory`), il pacchetto di `SimpleLoggerAdvisor` (`org.springframework.ai.chat.client.advisor`) e il suo costruttore senza argomenti.

`AssistenteConfig.java`

```java
package it.corso.ai;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class AssistenteConfig {

    // ChatMemory è auto-configurato (finestra di 20 messaggi, repository in memoria)
    @Bean
    ChatClient assistente(ChatClient.Builder builder, ChatMemory chatMemory, MagazzinoTools tools) {
        return builder
                .defaultSystem("Sei l'assistente del magazzino di un negozio di informatica. "
                        + "Per giacenze e prenotazioni usa SOLO i tool: non inventare numeri. "
                        + "Rispondi in italiano, in modo breve.")
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(chatMemory).build(),  // memoria: registrata a mano
                        new SimpleLoggerAdvisor())                             // log di richiesta e risposta
                .defaultTools(tools)                                           // tool sempre disponibili
                .build();
    }
}
```

`AssistenteController.java`

```java
package it.corso.ai;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ai")
class AssistenteController {

    private final ChatClient assistente;

    AssistenteController(ChatClient assistente) {
        this.assistente = assistente;
    }

    @PostMapping("/assistente/{sessione}")
    String chat(@PathVariable String sessione, @RequestBody String messaggio) {
        return assistente.prompt()
                .user(messaggio)
                // senza questo parametro la memoria lancia IllegalArgumentException
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, sessione))
                .call()
                .content();
    }
}
```

Aggiungere a `application.properties` per vedere i log degli advisor:

```properties
logging.level.org.springframework.ai.chat.client.advisor=DEBUG
```

Prova (riavviare l'applicazione prima, per ripartire dal magazzino iniziale):

```bash
curl -X POST -H "Content-Type: text/plain" -d "Ciao, sono Anna. Quanti MON-27 abbiamo?" http://localhost:8080/ai/assistente/anna
curl -X POST -H "Content-Type: text/plain" -d "Riservane 3 per me" http://localhost:8080/ai/assistente/anna
curl -X POST -H "Content-Type: text/plain" -d "Come mi chiamo?" http://localhost:8080/ai/assistente/anna
curl -X POST -H "Content-Type: text/plain" -d "Come mi chiamo?" http://localhost:8080/ai/assistente/luca
```

Output atteso (il testo varia):

```
Ciao Anna! Di MON-27 ne abbiamo 4 disponibili.
Fatto: ho riservato 3 pezzi di MON-27, ne resta 1.
Ti chiami Anna.
Non so come ti chiami: non me l'hai ancora detto.
```

Nei log, a livello `DEBUG`, compaiono richiesta e risposta di ogni chiamata (il formato esatto dipende dalla versione).

- Il bean `ChatClient` è costruito una sola volta, con system prompt, advisor e tool comuni a tutte le chiamate.
- `MessageChatMemoryAdvisor` aggiunge al prompt i messaggi già scambiati in quella conversazione e salva i nuovi: per questo "Riservane 3" capisce che si parla di MON-27.
- `ChatMemory.CONVERSATION_ID` prende il valore dal path (`anna`, `luca`): ID diversi, memorie separate. L'endpoint non conserva nulla: lo stato sta nel `ChatMemory`.
  **Semplificazione didattica:** qui l'ID lo sceglie il client nell'URL, così si vedono due conversazioni con due `curl`; ma chiunque può scrivere `/assistente/anna` e leggere la conversazione di Anna. In produzione l'ID si ricava lato server (utente autenticato, sessione), come nella regola di «Memory e Advisor insieme». Vale anche per `{utente}` dell'esercizio Difficile.
- `SimpleLoggerAdvisor` non cambia la risposta: osserva e scrive nei log. L'ordine degli advisor si controlla con `getOrder()` (valori bassi per primi).
- I tool del magazzino funzionano come nell'esempio Intermedio, ma qui sono registrati una volta sola con `defaultTools`.
- **Cosa succede se** togli `.advisors(a -> a.param(CONVERSATION_ID, ...))`? La memoria lancia `IllegalArgumentException` (HTTP 500): non esiste un ID di default.
- **Cosa succede se** riavvii l'applicazione? La memoria è in RAM e si azzera: Anna non è più riconosciuta (e il magazzino riparte dai valori iniziali). Per conservarla servirebbe un repository persistente (es. JDBC).
- **Cosa succede se** una conversazione supera i 20 messaggi? I più vecchi non vengono più inviati: dopo molti scambi il modello può "dimenticare" il nome dato all'inizio.
- **Cosa succede se** due persone usano lo stesso ID? Condividono la conversazione e vedono i dati l'una dell'altra: l'ID va deciso dal server, non lasciato alla scelta dell'utente.

## Esercizi

### Facile

**Obiettivo:** creare un endpoint `POST /ai/libro` che estrae i dati di un libro da una frase e li restituisce come oggetto JSON.
**Dati:** un record `Libro` con i campi `titolo`, `autore`, `anno`; la frase arriva nel body come testo semplice. Frase di prova: `Il nome della rosa è un romanzo di Umberto Eco pubblicato nel 1980.`
**Risultato atteso:** la chiamata restituisce `{"titolo":"Il nome della rosa","autore":"Umberto Eco","anno":1980}`. Con la frase `Se questo è un uomo è un libro di Primo Levi uscito nel 1947.` restituisce `{"titolo":"Se questo è un uomo","autore":"Primo Levi","anno":1947}`.

### Medio

**Obiettivo:** dare al modello un tool per calcolare i prezzi scontati e verificare che venga davvero chiamato.
**Dati:** una classe con il metodo `applicaSconto(double prezzo, double percentuale)`, che restituisce il prezzo scontato; un endpoint `GET /ai/sconto?domanda=...`. Il system prompt deve imporre di usare il tool per ogni calcolo. Il metodo deve scrivere una riga in console a ogni chiamata.
**Risultato atteso:** per `Un articolo costa 240 euro con sconto del 15%: quanto si paga?` la risposta indica 204 euro (il tool restituisce `204.0`); per `80 euro con sconto del 25%` indica 60 euro (`60.0`). In console compare una riga per ogni chiamata del tool.

### Difficile

**Obiettivo:** realizzare una chat con memoria separata per utente e osservare cosa succede quando la finestra è troppo piccola.
**Dati:** una `ChatMemory` con finestra di 4 messaggi (`MessageWindowChatMemory.builder().maxMessages(4).build()`, pacchetto `org.springframework.ai.chat.memory`), creata nel costruttore del controller e passata a `MessageChatMemoryAdvisor.builder(...)`; non definire un secondo bean `ChatMemory`, per non andare in conflitto con quello auto-configurato. Lavora in un progetto a parte o con un `ChatClient` distinto da quello dell'Avanzato, così non cambi la finestra dell'assistente. Un endpoint `POST /ai/chat/{utente}` che invia al modello il testo del body; un `ChatClient` con la memoria registrata e l'ID di conversazione preso dall'utente nel path. Scenario di prova, in ordine:

1. `anna`: `Mi chiamo Anna e adoro il basilico`
2. `luca`: `Mi chiamo Luca e adoro la pizza`
3. `anna` e poi `luca`: `Come mi chiamo e cosa adoro?`
4. tre messaggi di riempimento a `anna` (es. `Dimmi una curiosità sul mare`, `E sulla luna?`, `E sul vento?`)
5. `anna`: `Come mi chiamo e cosa adoro?`

**Risultato atteso:** al passo 3 ciascuno riceve i propri dati (Anna e il basilico, Luca e la pizza), senza scambi. Al passo 5 il modello in genere **non** sa più rispondere a `anna`. Scrivi in 2-3 righe perché, usando i concetti di finestra e di messaggi conteggiati (domanda e risposta contano entrambe).

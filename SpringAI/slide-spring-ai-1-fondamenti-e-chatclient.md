# Spring AI — Parte 1: fondamenti dell'AI, setup e ChatClient

## Spiegazione teorica

### Che cos'è l'intelligenza artificiale

- **AI** = sistemi software che svolgono compiti che di solito richiedono intelligenza umana: riconoscere immagini, capire testi, prendere decisioni.
- **Per capacità:**
  - *AI ristretta (narrow):* fa bene **un** compito (filtro antispam, raccomandazioni, chatbot). È tutta l'AI che esiste oggi.
  - *AI generale (AGI):* ipotetica, capace di qualsiasi compito intellettuale umano. Non esiste.
  - *Superintelligenza:* ipotetica, supera l'uomo. Solo teoria.
- **Per approccio:**
  - *AI simbolica:* regole scritte a mano dal programmatore ("se... allora...").
  - *Apprendimento automatico:* le regole le **impara** il sistema dai dati.
- ChatGPT, Llama e simili sono AI ristretta: eccellono nel linguaggio, ma non "capiscono" come una persona.

### Machine Learning

- **Idea:** invece di scrivere le regole, si danno al sistema molti esempi e si lascia che ricavi da solo il modello.
- Flusso: dati → **addestramento (training)** → **modello** → **inferenza** (il modello applicato a dati nuovi).
- Il modello è un file di parametri numerici, non codice con `if`.
- Qualità dei dati e qualità del modello vanno insieme: dati sbagliati o sbilanciati producono un modello sbagliato (*garbage in, garbage out*).
- Tre famiglie di apprendimento: **supervisionato**, **non supervisionato**, **per rinforzo** (un agente impara per tentativi, ricevendo premi o penalità; es. giochi, robotica).

### Supervised e Unsupervised Learning

- **Supervised:** i dati di training hanno già la **risposta corretta** (etichetta).
  - *Classificazione:* la risposta è una categoria (spam / non spam).
  - *Regressione:* la risposta è un numero (prezzo di una casa).
- **Unsupervised:** i dati **non** hanno etichette; il sistema cerca struttura da solo.
  - *Clustering:* raggruppa elementi simili (segmenti di clienti).
  - *Riduzione di dimensioni, anomalie:* semplifica i dati o trova ciò che "non torna".
- Regola pratica: hai le risposte giuste a disposizione? → supervised. Vuoi scoprire gruppi o anomalie? → unsupervised.
- Confusione tipica: "unsupervised" non significa "senza dati" né "senza controllo umano": significa senza etichette.

### Deep Learning

- Sottoinsieme del ML che usa **reti neurali profonde**: molti strati di "neuroni" artificiali che trasformano i dati passo dopo passo.
- Ogni strato impara caratteristiche più astratte (bordi → forme → volti).
- Funziona molto bene su dati non strutturati (immagini, audio, testo) ma richiede **molti dati** e **molta potenza di calcolo** (GPU).
- Gerarchia da ricordare: **AI ⊃ Machine Learning ⊃ Deep Learning** (un insieme dentro l'altro).
- Gli LLM sono reti neurali profonde basate sull'architettura *Transformer* (2017).

### Natural Language Processing (NLP)

- **NLP** = far elaborare al computer il linguaggio umano scritto o parlato.
- Compiti classici: classificazione di testi, analisi del sentimento, riconoscimento di entità (nomi, date, luoghi), traduzione, riassunto, risposta a domande.
- Prima degli LLM servivano modelli diversi per ogni compito; oggi un solo LLM li svolge quasi tutti, guidato dal **prompt**.
- Difficoltà del linguaggio: ambiguità, ironia, contesto, lingue diverse.

### Large Language Model (LLM)

- **LLM** = modello di deep learning addestrato su enormi quantità di testo per **predire il prossimo token**.
- **Token:** pezzo di testo (parola o parte di parola). Costi e limiti si misurano in token, non in parole.
- **Finestra di contesto:** quanti token il modello può "vedere" in una richiesta (prompt + cronologia + risposta). Oltre il limite, il testo viene perso o la richiesta fallisce.
- **Stateless:** il modello non ricorda nulla tra una richiesta e l'altra; ogni chiamata parte da zero.
- **Non deterministico:** la stessa domanda può dare risposte diverse (parametro *temperature*: più alto = più variabile).
- Esempi: GPT (OpenAI), Llama (Meta), Claude (Anthropic), Gemini (Google).

### Generative AI e suoi limiti

- **Generative AI** = modelli che **creano** contenuti nuovi (testo, immagini, audio, codice) invece di limitarsi a classificare o predire un valore.
- Gli LLM generano testo plausibile, **non verificato**.
- **Limiti da conoscere prima di usarli in un'applicazione:**
  - *Allucinazioni:* risposte false dette con sicurezza.
  - *Conoscenza datata:* il modello sa solo ciò che c'era nel training.
  - *Nessun accesso ai tuoi dati* privati o in tempo reale.
  - *Costo e latenza:* ogni chiamata costa e richiede secondi.
  - *Privacy:* i dati inviati a un provider escono dalla tua applicazione.
- Per superare questi limiti servono gli strumenti dei prossimi moduli: structured output, tools, memory, RAG.

### Spring AI: che cos'è e come è fatto

- **Spring AI** = framework dell'ecosistema Spring per integrare modelli AI in applicazioni Java con gli stessi principi di Spring (auto-configurazione, dependency injection, astrazioni portabili).
- **Problema che risolve:** ogni provider ha API, SDK e formati diversi. Spring AI offre **un'unica API** e permette di cambiare provider modificando la configurazione.
- **Pezzi principali:**
  - `ChatModel`: interfaccia di basso livello verso il modello.
  - `ChatClient`: API fluente di alto livello (quella che useremo).
  - `Prompt` / messaggi: system, user, assistant.
  - **Advisors:** intercettori della richiesta (memoria, RAG, log).
  - **Tools**, **Structured Output**, **VectorStore** / **EmbeddingModel**.
- Versioni: **Spring AI 2.0.x** richiede **Spring Boot 4.0 / 4.1**, Spring Framework 7 e Java 21 (nel nostro corso: JDK 21). Per Spring Boot 3.5 esiste la linea 1.1.x.

### Setup del progetto

- **Dipendenze Maven:** il BOM `spring-ai-bom` (gestisce le versioni) + lo starter `spring-ai-starter-model-openai` + `spring-boot-starter-webmvc` per le API REST. In Spring Boot 4 lo starter `spring-boot-starter-web` si chiama `spring-boot-starter-webmvc`.
- **Perché lo starter OpenAI anche per Groq:** Groq espone un'API compatibile con OpenAI, quindi si usa lo stesso starter cambiando URL, chiave e modello.
- **Proprietà (2.0):**
  - `spring.ai.openai.api-key` — chiave del provider.
  - `spring.ai.openai.base-url` — default `api.openai.com`; per Groq `https://api.groq.com/openai/v1`.
  - `spring.ai.openai.chat.model` — nome del modello.
- **La chiave API non va mai nel codice né su Git:** si legge da variabile d'ambiente (`${GROQ_API_KEY}`) oppure dal file `.env` (procedura sotto).
- Se la chiave manca o è sbagliata, l'applicazione parte ma la prima chiamata fallisce (o parte male, a seconda del provider).

### Procedura: chiave Groq e primo avvio

1. Crea una chiave gratuita su [console.groq.com/keys](https://console.groq.com/keys) → *Create API Key*. Copiala subito: la console non la mostra una seconda volta.
2. Nella cartella `corso-spring-ai/` crea il file `.env` a partire dal modello:
   ```bash
   cp .env.example .env
   ```
3. Apri `.env` e sostituisci il segnaposto con la tua chiave:
   ```properties
   GROQ_API_KEY=gsk_...
   ```
4. Non condividere `.env`: non incollarlo in chat, non mostrarlo in aula, non committarlo. Il file è già in `.gitignore`, quindi Git lo ignora.
5. Avvia dalla cartella `corso-spring-ai/`, perché `.env` si cerca nella cartella di lavoro:
   ```bash
   ./mvnw spring-boot:run
   ```
6. Verifica da un secondo terminale:
   ```bash
   curl http://localhost:8080/ai/provider
   # {"profilo":"groq","modello":"openai/gpt-oss-120b"}

   curl "http://localhost:8080/ai/chat?message=Ciao"
   ```

Problemi comuni:
- **Errore 401, "chiave non valida":** la chiave è sbagliata o incompleta nel `.env`. Dopo ogni modifica al file, riavvia l'applicazione.
- **L'avvio riesce ma la chiamata fallisce:** la chiave manca o il provider non è raggiungibile. Guarda il log della console.
- **Preferisci le variabili d'ambiente?** `export GROQ_API_KEY=...` prima di `./mvnw spring-boot:run` funziona lo stesso. Spring Boot dà priorità alla variabile d'ambiente rispetto al file `.env`.

### Groq o OpenAI: come scegliere

| | Groq | OpenAI |
| --- | --- | --- |
| Costo | piano gratuito con limiti giornalieri | a pagamento, pochi euro bastano per il corso |
| Modelli | open (gpt-oss, Llama, ecc.), es. `openai/gpt-oss-120b` | GPT, es. `gpt-5-mini` (default Spring AI 2.0) |
| Compatibilità | non completamente compatibile con l'API OpenAI | nativa |
| Tools | solo con modelli che li supportano | ampio supporto |
| Multimodale | non supportato da Spring AI con Groq | sì |
| Embeddings | non disponibili su Groq | disponibili |

- I limiti del piano gratuito Groq (richieste e token al giorno) cambiano: controlla la console Groq.
- Con i modelli `gpt-5*` l'opzione *temperature* non è supportata: non impostarla.

### ChatClient

- Punto d'ingresso per parlare col modello. Spring Boot auto-configura un `ChatClient.Builder` da iniettare nel costruttore: `builder.build()` produce il client.
- **Catena fluente:** `prompt()` → `system(...)` / `user(...)` → `call()` → `content()`.
  - `system`: istruzioni sul ruolo e sul comportamento ("sei un tutor Java...").
  - `user`: la richiesta dell'utente.
  - `call()` = chiamata sincrona; `stream()` = risposta a pezzi (Flux).
  - `content()` → `String`; `chatResponse()` → risposta completa con **metadati** (token usati, ecc.).
- **Default del client:** `builder.defaultSystem("...")` vale per tutte le chiamate.
- **Template di prompt:** `user(u -> u.text("Spiega {argomento}").param("argomento", x))`. Le `{graffe}` sono segnaposto: attenzione se il testo contiene graffe letterali (codice, JSON).
- **Quando usare stream:** risposte lunghe e interfacce che mostrano il testo mentre arriva.

## Esempi teorici di utilizzo

- **Filtro antispam per una casella aziendale (supervised, classificazione).** Un'azienda ha migliaia di email già segnate come "spam" o "legittima". Scrivere regole a mano non regge: gli spammer cambiano parole ogni giorno. Un modello supervised impara dagli esempi etichettati e classifica le email nuove.
- **Segmentazione dei clienti di un negozio online (unsupervised, clustering).** Il marketing non sa quali gruppi di clienti esistano e non ha etichette. Il clustering raggruppa i clienti per acquisti simili e fa emergere segmenti ("sconti", "premium", "occasionali") su cui costruire campagne diverse.
- **Analisi delle recensioni di un hotel (NLP).** Centinaia di recensioni a settimana, in più lingue. Un sistema NLP classifica il sentimento e estrae i temi ricorrenti (pulizia, colazione, rumore) senza che qualcuno le legga una per una.
- **Assistente interno per i dipendenti (LLM + Spring AI).** Un'azienda Java vuole un assistente che risponda alle domande dei colleghi. Con Spring AI basta un endpoint REST e un `ChatClient`: stesso stile di ogni altro servizio Spring, senza scrivere a mano chiamate HTTP verso il provider.
- **Bozza di risposte per l'assistenza clienti (Generative AI).** Il supporto riceve richieste ripetitive. Il modello genera una bozza di risposta che l'operatore rivede e invia: si risparmia tempo, ma c'è sempre un controllo umano per evitare allucinazioni.
- **Cambiare provider senza riscrivere il codice (valore di Spring AI).** Il team sviluppa con Groq gratuito e passa a OpenAI in produzione. Poiché il codice usa solo `ChatClient`, cambia solo la configurazione.
- **Quando NON usare un LLM.** Calcolo di un importo in fattura, saldo di un conto, orario in tempo reale: servono risultati esatti e dati aggiornati. Un LLM può sbagliare in modo convincente; qui si usa codice tradizionale (o si fa chiamare il codice al modello con i *tools*, modulo successivo).

## Esempi di codice

> Gli esempi di questo file **non sono stati eseguiti** (nell'ambiente di lavoro non era possibile scaricare le dipendenze Maven né chiamare il provider). Import, firme e versioni sono stati verificati sulla reference Spring AI 2.0. Prima della lezione: provarli con una chiave valida.

### Base

Progetto minimo: `pom.xml`, configurazione e un endpoint che inoltra la domanda al modello. Questi tre file sono la base di tutti gli esempi successivi.

`pom.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>

  <parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>4.0.0</version> <!-- usare l'ultima patch 4.0.x o 4.1.x -->
  </parent>

  <groupId>it.corso</groupId>
  <artifactId>corso-ai</artifactId>
  <version>0.0.1-SNAPSHOT</version>

  <properties>
    <java.version>21</java.version>
    <spring-ai.version>2.0.1</spring-ai.version> <!-- ultima 2.0.x disponibile -->
  </properties>

  <dependencyManagement>
    <dependencies>
      <dependency>
        <groupId>org.springframework.ai</groupId>
        <artifactId>spring-ai-bom</artifactId>
        <version>${spring-ai.version}</version>
        <type>pom</type>
        <scope>import</scope>
      </dependency>
    </dependencies>
  </dependencyManagement>

  <dependencies>
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-webmvc</artifactId>
    </dependency>
    <dependency>
      <groupId>org.springframework.ai</groupId>
      <artifactId>spring-ai-starter-model-openai</artifactId>
    </dependency>
  </dependencies>

  <build>
    <plugins>
      <plugin>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-maven-plugin</artifactId>
      </plugin>
    </plugins>
  </build>
</project>
```

`src/main/resources/application.properties` (variante Groq)

```properties
spring.ai.openai.api-key=${GROQ_API_KEY}
spring.ai.openai.base-url=https://api.groq.com/openai/v1
spring.ai.openai.chat.model=openai/gpt-oss-120b
spring.ai.openai.chat.replay-reasoning-content=false

# Variante OpenAI: sostituire le righe sopra con
# spring.ai.openai.api-key=${OPENAI_API_KEY}
# spring.ai.openai.chat.model=gpt-5-mini
```

`src/main/java/it/corso/ai/CorsoAiApplication.java` e `ChatController.java`

```java
package it.corso.ai;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
public class CorsoAiApplication {
    public static void main(String[] args) {
        SpringApplication.run(CorsoAiApplication.class, args);
    }
}

@RestController
@RequestMapping("/ai")
class ChatController {

    private final ChatClient chatClient;

    // Spring inietta il Builder auto-configurato dallo starter
    ChatController(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    @GetMapping("/chat")
    String chat(@RequestParam String message) {
        return chatClient.prompt()   // inizia la richiesta
                .user(message)       // messaggio dell'utente
                .call()              // chiamata sincrona al modello
                .content();          // solo il testo della risposta
    }
}
```

Avvio e prova (prima: `export GROQ_API_KEY=...`):

```bash
mvn spring-boot:run
curl "http://localhost:8080/ai/chat?message=Spiega%20in%20due%20righe%20cos%27%C3%A8%20un%20LLM"
```

Output atteso: un breve testo in italiano sugli LLM. Il testo varia a ogni chiamata.

- Lo starter crea un `ChatModel` dalle proprietà `spring.ai.openai.*`; il `ChatClient.Builder` lo usa.
- `base-url` e `api-key` fanno la differenza tra Groq e OpenAI: il codice Java non cambia.
- La catena `prompt().user().call().content()` è la forma minima; ogni metodo restituisce l'oggetto successivo.
- La chiave arriva dalla variabile d'ambiente: nel repository non compare nessun segreto.
- Se la chiave non è impostata, l'avvio fallisce con un errore sulla proprietà `api-key` non risolta.

### Intermedio

Un tutor con ruolo fisso (`defaultSystem`), un template con parametro e la lettura dei metadati (token usati). Dipende dal progetto dell'esempio Base (stesso `pom.xml` e stesse proprietà).

```java
package it.corso.ai;

import java.util.Map;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ai")
class RipassoController {

    private final ChatClient chatClient;

    RipassoController(ChatClient.Builder builder) {
        // Il ruolo vale per tutte le chiamate di questo client
        this.chatClient = builder
                .defaultSystem("Sei un tutor di Java per principianti. "
                        + "Rispondi in italiano, al massimo 5 righe, con un esempio di una riga di codice.")
                .build();
    }

    @GetMapping("/ripasso")
    Map<String, Object> ripasso(@RequestParam String argomento) {
        ChatResponse risposta = chatClient.prompt()
                // {argomento} è un segnaposto del template
                .user(u -> u.text("Spiega {argomento} a uno studente alle prime armi")
                        .param("argomento", argomento))
                .call()
                .chatResponse();   // risposta completa, non solo il testo

        return Map.of(
                "testo", risposta.getResult().getOutput().getText(),
                "tokenTotali", risposta.getMetadata().getUsage().getTotalTokens());
    }
}
```

Prova:

```bash
curl "http://localhost:8080/ai/ripasso?argomento=le%20interfacce"
```

Output atteso (il testo varia):

```json
{"testo":"Un'interfaccia definisce un contratto di metodi che le classi devono implementare...\nEsempio: interface Animale { void verso(); }","tokenTotali":187}
```

- `defaultSystem` fissa tono, lingua e lunghezza: senza, il modello risponderebbe con stili diversi a ogni chiamata.
- `.text(...)` + `.param(...)` sostituisce `{argomento}` prima dell'invio.
- `chatResponse()` restituisce testo **e** metadati: serve per tenere sotto controllo i costi (token).
- `getResult().getOutput().getText()` estrae il testo dalla risposta completa.
- **Cosa succede se** il parametro contiene graffe (es. `Map<String,{x}>`)? Come valore di `.param()` è sicuro; se invece scrivi graffe letterali nel testo del template, vengono lette come segnaposto e la chiamata dà errore di rendering.
- **Cosa succede se** il provider non restituisce i dati di utilizzo? `getTotalTokens()` può essere 0 o un valore non significativo: non usarlo per la fatturazione.

### Avanzato

Risposta in streaming (SSE) con gestione degli errori durante il flusso. Dipende dal progetto dell'esempio Base.

```java
package it.corso.ai;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/ai")
class StoriaController {

    private final ChatClient chatClient;

    StoriaController(ChatClient.Builder builder) {
        this.chatClient = builder
                .defaultSystem("Sei un narratore. Scrivi sempre in italiano.")
                .build();
    }

    // text/event-stream: il browser/curl riceve i pezzi appena arrivano
    @GetMapping(value = "/storia", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    Flux<String> storia(@RequestParam String tema) {
        return chatClient.prompt()
                .user(u -> u.text("Racconta in 6 frasi una storia su {tema}")
                        .param("tema", tema))
                .stream()                // risposta a pezzi invece di call()
                .content()               // Flux<String>: un pezzo di testo alla volta
                // un errore a metà flusso non si può più trasformare in HTTP 502:
                // lo comunichiamo come ultimo pezzo
                .onErrorResume(e -> Flux.just("\n[errore: provider AI non disponibile]"));
    }
}
```

Prova (`-N` disattiva il buffer di curl):

```bash
curl -N "http://localhost:8080/ai/storia?tema=un%20robot%20bibliotecario"
```

Output atteso (a pezzi, il testo varia):

```
data:C'era
data: una
data: volta
data: un robot...
```

- `stream()` al posto di `call()` restituisce un `Flux`: il primo pezzo arriva subito, senza aspettare tutta la risposta.
- `produces = TEXT_EVENT_STREAM_VALUE` dice a Spring MVC di inviare i pezzi come eventi SSE.
- `content()` sul flusso estrae solo il testo di ogni pezzo.
- `onErrorResume` gestisce errori che arrivano **dopo** l'inizio della risposta: lo status HTTP è già 200 e non può più cambiare.
- **Cosa succede se** la chiave è sbagliata? L'errore arriva all'inizio del flusso e l'utente vede solo il messaggio di `onErrorResume`: senza di esso la connessione si chiude e il client riceve una risposta vuota o troncata.
- **Cosa succede se** usi `.entity(...)` con `stream()`? Non è supportato: l'oggetto tipizzato si ottiene solo con `call()` (vedi Parte 2).

## Esercizi

### Facile

**Obiettivo:** creare un endpoint `GET /ai/traduci` che traduce un testo nella lingua richiesta.
**Dati:** parametri `testo` e `lingua`; il prompt deve usare un template con due segnaposto.
**Risultato atteso:** `GET /ai/traduci?testo=Buongiorno a tutti&lingua=inglese` restituisce solo la traduzione (es. `Good morning everyone`), senza spiegazioni aggiuntive.

### Medio

**Obiettivo:** creare un endpoint `GET /ai/commit` che migliora un messaggio di commit Git.
**Dati:** il client deve avere un `defaultSystem` che lo definisce "revisore di commit message": risposta in inglese, imperativo, massimo 72 caratteri, una sola riga. Input di prova: `fix bug login che non funzionava`.
**Risultato atteso:** JSON con due campi, `messaggio` (la riga proposta, es. `Fix login failure`) e `tokenTotali` (numero intero maggiore di 0).

### Difficile

**Obiettivo:** creare un endpoint `POST /ai/riassunto` che restituisce in streaming un riassunto in 3 punti elenco di un testo ricevuto nel body.
**Dati:** body di prova (testo libero di almeno 8 righe a scelta); risposta come `text/event-stream`; il system prompt deve imporre l'italiano e un massimo di 3 punti.
**Risultato atteso:** con `curl -N -X POST --data-binary @testo.txt http://localhost:8080/ai/riassunto` i punti compaiono progressivamente nel terminale. Se spegni la connessione di rete o usi una chiave errata, l'ultimo pezzo ricevuto è un messaggio di errore leggibile e non una risposta vuota.

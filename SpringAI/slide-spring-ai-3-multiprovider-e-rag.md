# Spring AI — Parte 3: multi-provider con Profiles e RAG

## Spiegazione teorica

### Perché usare più provider
- **Sviluppo e produzione hanno esigenze diverse:** in sviluppo conta il costo (Groq gratuito), in produzione contano qualità, limiti e garanzie (OpenAI).
- **Limiti e disservizi:** ogni provider ha limiti di richieste e token, e a volte è offline. Dipendere da un solo provider è un rischio.
- **Modelli diversi per compiti diversi:** un modello piccolo e veloce per risposte semplici, uno grande per quelle complesse.
- **Privacy e compliance:** alcuni dati possono andare solo a certi provider (o a modelli in esecuzione in casa).
- **Evitare il lock-in:** se il codice non è legato a un fornitore, cambiarlo costa una modifica di configurazione e non una riscrittura.

### Come funziona in Spring AI
- Molti provider espongono un'API **compatibile con OpenAI**: stesso formato delle richieste, cambiano solo indirizzo, chiave e modello.
- Con lo starter `spring-ai-starter-model-openai` bastano tre proprietà:
  - `spring.ai.openai.base-url` — default `api.openai.com`; per Groq `https://api.groq.com/openai/v1`.
  - `spring.ai.openai.api-key` — la chiave del provider.
  - `spring.ai.openai.chat.model` — il modello (default `gpt-5-mini`).
- Il codice Java (`ChatClient`) **non cambia**: è la configurazione a decidere chi risponde.
- **Differenze di Groq da ricordare:** non è completamente compatibile con l'API OpenAI, niente multimodale, tools solo su modelli che li supportano, **niente embeddings**, limiti del piano gratuito variabili. Con Groq serve `spring.ai.openai.chat.replay-reasoning-content=false`.
- **Nomi dei modelli:** i modelli Groq cambiano e vengono ritirati. `openai/gpt-oss-120b` e `openai/gpt-oss-20b` sono stati dismessi ad agosto 2026; i sostituti indicati da Groq sono `openai/gpt-oss-120b` e `openai/gpt-oss-20b`. Se ricevi «model not found» o «modello non disponibile», controlla la pagina delle deprecazioni di Groq (`console.groq.com/docs/deprecations`) e aggiorna `spring.ai.openai.chat.model`.
- Con i modelli `gpt-5*` la *temperature* non è supportata: non impostarla.

### Spring Boot Profiles
- Un **profilo** è un nome (`groq`, `openai`, `test`...) che attiva configurazione specifica per un ambiente.
- **File per profilo:** `application-{profilo}.properties` viene caricato **in aggiunta** a `application.properties` e vince in caso di proprietà uguali.
- **Attivazione**, dalla meno alla più "forte":
  - `spring.profiles.active=groq` dentro `application.properties`;
  - variabile d'ambiente `SPRING_PROFILES_ACTIVE`;
  - da riga di comando con Maven: `-Dspring-boot.run.profiles=openai`.
- `@Profile("groq")` su una classe o un bean: esiste solo se quel profilo è attivo.
- **Segreti:** le chiavi restano in variabili d'ambiente (`${GROQ_API_KEY}`), mai nei file committati.

### Strategie di switch: all'avvio o a runtime
| | Switch all'avvio (profilo) | Switch a runtime (per richiesta) |
|---|---|---|
| Come | un profilo attivo, un solo modello | due (o più) `ChatClient` sempre pronti |
| Cambio provider | riavvio dell'applicazione | parametro della richiesta |
| Chiavi necessarie | solo quella del profilo attivo | tutte, all'avvio |
| Adatto a | ambienti (sviluppo, test, produzione) | confronti, failover, scelta per costo |

- Si possono combinare: il profilo decide il comportamento di default, il client giusto viene scelto per richiesta.
- **Attenzione:** per raccogliere più `ChatClient` in un oggetto non dichiarare un `Map<String, ChatClient>` come `@Bean`. Quando Spring deve iniettare una `Map<String, X>`, la riempie con **tutti i bean di tipo `X`** (nome → bean) e non usa la tua `Map`: il risultato non è quello atteso. Si usa un `record` con un campo per provider.

### RAG: il problema
- Un LLM sa solo ciò che c'era nel training: **non conosce i tuoi documenti** (regolamenti, manuali, contratti) né ciò che è successo dopo.
- Sui dati che non ha, **inventa** (allucinazioni) con tono sicuro.
- **RAG** = *Retrieval-Augmented Generation*: prima si **recuperano** i passaggi rilevanti dai tuoi documenti, poi li si **aggiunge al prompt**, poi il modello **genera** la risposta basandosi su quelli.
- Idea chiave: non si insegna nulla al modello; gli si dà, a ogni domanda, il materiale giusto da leggere.
- Perché non mettere tutti i documenti nel prompt? Finestra di contesto limitata, costo in token e risposte peggiori con troppo testo.

### La pipeline RAG
- **Ingestion** (una volta, o quando i documenti cambiano):
  - *lettura* dei documenti (PDF, Word, testo);
  - *suddivisione in chunk* (pezzi di testo di dimensione gestibile);
  - *embedding* di ogni chunk (trasformazione in vettore numerico);
  - *salvataggio* nel database vettoriale.
- **Query** (a ogni domanda):
  - embedding della domanda → ricerca dei chunk più simili → aumento del prompt con quei chunk → generazione della risposta.
- **In Spring AI:** `DocumentReader` (es. `TikaDocumentReader`), `DocumentTransformer` (es. `TokenTextSplitter`), `DocumentWriter` (il `VectorStore`).
- L'ingestion e la query devono usare **lo stesso modello di embedding**.

### Embedding e similarità
- **Embedding** = un testo diventa un **vettore** (una lista di numeri) che ne rappresenta il significato.
- Testi con significato simile hanno vettori **vicini**, anche se usano parole diverse ("ferie" ≈ "giorni di vacanza").
- **Similarità del coseno:** misura l'angolo tra due vettori. Vicino a 1 = molto simili, vicino a 0 = non correlati.
- **Dimensione** del vettore: dipende dal modello (il modello locale di esempio produce 384 numeri).
- Un embedding non è una traduzione né una risposta: serve solo a **confrontare** testi.
- Il modello di embedding è diverso dal modello di chat. Groq non offre embeddings: servono un modello locale o un altro provider.

### Database vettoriali
- **VectorStore** = archivio che salva testi + vettori + metadati e risponde a "dammi i più simili a questo".
- Differenza dal database classico: SQL cerca per **uguaglianza**, il database vettoriale cerca per **vicinanza di significato**.
- Per milioni di vettori usa **indici approssimati**: più veloci, a costo di qualche risultato mancato.
- **Metadati e filtri:** ogni documento può avere attributi (reparto, anno, lingua) e la ricerca si restringe con espressioni come `"reparto == 'HR' && anno >= 2025"`.
- In Spring AI il `VectorStore` **non genera** gli embedding: li crea l'`EmbeddingModel`.
- `SimpleVectorStore`: in memoria, solo per prove e demo, si svuota al riavvio. In produzione: pgvector, Qdrant, Milvus, Redis, Chroma e altri.

### Similarity search e tecniche di recupero
- **topK:** quanti risultati restituire (default 4). Troppo basso = si perde l'informazione; troppo alto = rumore nel prompt.
- **Soglia di similarità** (da 0 a 1; default 0.0 = accetta tutto): scarta i risultati poco rilevanti. Troppo alta = nessun risultato; troppo bassa = risultati fuori tema.
- **Dimensione dei chunk e sovrapposizione:** chunk troppo piccoli perdono contesto, troppo grandi diluiscono il significato; una piccola sovrapposizione evita di tagliare una frase a metà.
- **Filtri sui metadati** per restringere la ricerca.
- **Tecniche avanzate:** *re-ranking* (riordinare i risultati con un secondo modello), *riscrittura della query* (rendere la domanda più adatta alla ricerca), **RAG modulare** (componenti separati per trasformare la query, recuperare, unire).
- Non esistono valori giusti in assoluto: topK, soglia e chunk si **calibrano** provando con domande reali.
- **Come calibrare soglia e topK** (4 passi):
  1. prepara 5-10 domande con risposta nei documenti e 3-5 fuori tema;
  2. per ciascuna osserva i punteggi con una ricerca senza soglia (come `/rag/cerca`);
  3. annota il punteggio più basso tra i passaggi giusti e il più alto tra quelli sbagliati;
  4. metti la soglia in mezzo e riprova.
- Un passaggio pertinente raramente ha un punteggio vicino a 1: conta la **distanza** tra giusti e sbagliati, non il valore assoluto.
- **Se i due gruppi si sovrappongono** nessuna soglia basta: il limite è il modello di embedding (per esempio perché conosce poco l'italiano). Si cambia modello (multilingua o embedding OpenAI) oppure si dichiara il limite.

### RAG con Spring AI
- **Artefatti da aggiungere** (versioni gestite dal BOM):
  - `spring-ai-vector-store` — `VectorStore`, `SimpleVectorStore`;
  - `spring-ai-vector-store-advisor` — `QuestionAnswerAdvisor`;
  - `spring-ai-starter-model-transformers` — embedding locale (ONNX);
  - `spring-ai-rag` — RAG modulare (`RetrievalAugmentationAdvisor`), non usato negli esempi.
- **`QuestionAnswerAdvisor`:** intercetta la richiesta, cerca nel `VectorStore` e aggiunge i testi trovati al prompt. Si configura con `SearchRequest` (topK, soglia) e si passa con `defaultAdvisors(...)` o per chiamata con `.advisors(...)`.
- **Conoscere le fonti usate:** `QuestionAnswerAdvisor` non le restituisce in modo documentato. Per mostrarle si ripete `similaritySearch` con lo **stesso** `SearchRequest` (stesso topK, stessa soglia) e si leggono i testi trovati.
- **Contesto vuoto:** se quella ricerca restituisce una lista vuota, puoi rispondere «Non lo so» **senza chiamare il modello**: è l'unico modo deterministico. Il comportamento di `QuestionAnswerAdvisor` quando non trova nulla non è documentato, e un system prompt «rispondi solo dal contesto» non è una garanzia.
- Il template con cui l'advisor compone il prompt si può personalizzare con `.promptTemplate(...)`. `RetrievalAugmentationAdvisor` (RAG modulare) per impostazione predefinita non risponde se non trova documenti (da riconfermare sulla reference).
- **Embedding locale:** modello predefinito `sentence-transformers/all-MiniLM-L6-v2` (384 dimensioni, circa 80 MB, nel classpath). Si seleziona con `spring.ai.model.embedding=transformers`; con lo starter OpenAI presente, senza questa proprietà ci sarebbero due `EmbeddingModel`.
- **Attenzione alla lingua:** il modello predefinito è addestrato soprattutto su inglese; in italiano la qualità cala. Alternative: un modello multilingua ONNX (`spring.ai.embedding.transformer.onnx.model-uri` e `...tokenizer.uri`) o gli embedding OpenAI.
- Il `VectorStore` si usa anche direttamente: `add(List<Document>)`, `similaritySearch(SearchRequest)`, `delete(...)`.

### Limiti del RAG e alternative
- **Limiti:**
  - la qualità della risposta dipende dalla qualità dei chunk recuperati: se il passaggio giusto non viene trovato, il modello non può usarlo;
  - il modello può comunque rispondere **fuori dal contesto**: serve un system prompt che lo vieti, e va verificato;
  - **prompt injection:** un documento può contenere istruzioni ("ignora le regole precedenti...") che il modello legge come se fossero del tuo prompt; i documenti non fidati vanno trattati con cautela;
  - i chunk recuperati vengono **inviati al provider**: valgono le regole di privacy.
- **Confronto con le alternative:**

| | Quando conviene | Limite |
|---|---|---|
| **RAG** | molti documenti, che cambiano; servono risposte con fonte | pipeline da costruire e calibrare |
| **Prompt lungo** | pochi testi, che entrano nella finestra | costo e limiti di contesto |
| **Fine-tuning** | cambiare stile o comportamento del modello | costoso, non adatto a dati che cambiano |

- Se i dati sono **strutturati** (tabelle, importi), meglio SQL o un *tool* (Parte 2): il RAG è per testo libero.

## Esempi teorici di utilizzo

- **Assistente HR sul regolamento aziendale (RAG).** I dipendenti chiedono ogni giorno all'ufficio HR le stesse cose: ferie, rimborsi, lavoro da remoto. Il regolamento è un documento privato che il modello non conosce. Con il RAG, la domanda recupera i paragrafi pertinenti e il modello risponde solo in base a quelli, riducendo le domande all'ufficio e il rischio di risposte inventate.
- **Helpdesk su documentazione di prodotto (RAG con metadati).** Un'azienda software ha centinaia di pagine di manuale per più versioni del prodotto. Il supporto fatica a trovare la pagina giusta. Con chunk indicizzati e un filtro sui metadati (es. versione), l'assistente recupera solo i passaggi della versione corretta e cita le fonti.
- **Ricerca semantica su contratti (embedding e similarità).** Uno studio legale cerca clausole sulla "risoluzione anticipata", ma nei contratti compaiono "recesso", "scioglimento", "disdetta". Una ricerca per parola chiave le perde; la ricerca per similarità di significato le trova, anche senza generare risposte.
- **Quando il RAG non serve (alternative).** Il saldo di un conto, le giacenze di magazzino, lo stato di un ordine sono dati strutturati e sempre aggiornati: servono SQL o un *tool* con risultato esatto. Se invece il testo è poco (una pagina di regole), basta metterlo nel prompt di sistema, senza vettori né chunk.
- **Sviluppo su Groq gratuito, produzione su OpenAI (profili).** Il team sviluppa e prova senza spese su Groq; in produzione vuole un modello più capace e limiti più larghi. Con due profili, `groq` e `openai`, ogni ambiente parte con la sua configurazione e il codice Java resta identico.
- **Failover quando un provider ha limiti o disservizi (switch a runtime).** Un servizio di assistenza non può restare muto se il provider principale è offline o ha esaurito la quota. Con due `ChatClient` sempre pronti, l'applicazione può passare all'altro provider per quella richiesta, senza riavvio.
- **Compliance e privacy dei dati (provider e RAG).** Una clinica non può inviare cartelle e documenti interni a qualsiasi servizio esterno. Deve scegliere con cura quale provider riceve i dati, usare embedding locali per non far uscire i testi durante l'indicizzazione e ricordare che i chunk recuperati finiscono comunque nel prompt inviato al modello di chat.

## Esempi di codice

> Gli esempi di questo file **non sono stati eseguiti** (nell'ambiente di lavoro non era possibile scaricare le dipendenze Maven né chiamare i provider). Import, firme e versioni sono stati verificati sulla reference Spring AI 2.0, con alcune eccezioni segnalate nelle righe di contesto come "da verificare". Prima della lezione: provarli con chiavi valide.

### Base

Switch con i **Profiles**: la configurazione di ogni provider va in un file dedicato e un endpoint mostra quale profilo e quale modello sono attivi. Dipende dal progetto della Parte 1 (stesso `pom.xml`); le proprietà `spring.ai.openai.*` dell'`application.properties` della Parte 1 vengono **spostate** nei file di profilo.

`src/main/resources/application.properties`
```properties
# Profilo attivo di default
spring.profiles.active=groq
```

`src/main/resources/application-groq.properties`
```properties
spring.ai.openai.api-key=${GROQ_API_KEY}
spring.ai.openai.base-url=https://api.groq.com/openai/v1
spring.ai.openai.chat.model=openai/gpt-oss-120b
spring.ai.openai.chat.replay-reasoning-content=false
```

`src/main/resources/application-openai.properties`
```properties
# base-url non serve: il default è api.openai.com
spring.ai.openai.api-key=${OPENAI_API_KEY}
spring.ai.openai.chat.model=gpt-5-mini
```

`src/main/java/it/corso/ai/ProviderController.java`
```java
package it.corso.ai;

import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ai")
class ProviderController {

    private final Environment env;   // accesso a profili e proprietà

    ProviderController(Environment env) {
        this.env = env;
    }

    @GetMapping("/provider")
    InfoProvider provider() {
        String profilo = String.join(",", env.getActiveProfiles());
        String modello = env.getProperty("spring.ai.openai.chat.model");
        return new InfoProvider(profilo, modello);
    }
}

record InfoProvider(String profilo, String modello) {}
```

Avvio e prova:
```bash
# terminale 1
export GROQ_API_KEY=...
mvn spring-boot:run
# terminale 2
curl http://localhost:8080/ai/provider

# stesso codice, altro provider: ferma l'applicazione (Ctrl+C) e riavviala
# terminale 1
export OPENAI_API_KEY=...
mvn spring-boot:run -Dspring-boot.run.profiles=openai
# terminale 2
curl http://localhost:8080/ai/provider
```

Output atteso:
```json
{"profilo":"groq","modello":"openai/gpt-oss-120b"}
{"profilo":"openai","modello":"gpt-5-mini"}
```

- `application.properties` sceglie il profilo; `application-{profilo}.properties` porta chiave, indirizzo e modello di quel provider.
- Per OpenAI non serve `base-url` (il default è già `api.openai.com`) né `replay-reasoning-content`, specifica di Groq.
- `-Dspring-boot.run.profiles=openai` ha la precedenza su `spring.profiles.active` del file: non serve modificare nulla.
- `Environment` legge il profilo attivo e le proprietà risolte: è il modo più semplice per verificare "chi sta rispondendo".
- L'endpoint `/ai/chat` della Parte 1 non cambia di una riga: usa il provider del profilo attivo.

### Intermedio

Switch **a runtime**: due `ChatClient` costruiti a mano, uno per Groq e uno per OpenAI, scelti con un parametro della richiesta. Dipende dal progetto della Parte 1; sostituisci l'`application.properties` con la sola riga indicata (i file di profilo dell'esempio Base non servono qui). Entrambe le variabili d'ambiente devono essere impostate. Dopo questa sostituzione `/ai/chat` della Parte 1 risponde con il modello di default OpenAI (`gpt-5-mini`), non più con Groq.

`src/main/resources/application.properties`
```properties
# Lo starter crea comunque un suo modello di default e richiede questa proprietà
spring.ai.openai.api-key=${OPENAI_API_KEY}
```

`src/main/java/it/corso/ai/MultiProviderConfig.java`
```java
package it.corso.ai;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class MultiProviderConfig {

    @Bean
    ClientiAi clientiAi(@Value("${GROQ_API_KEY}") String groqKey,
                        @Value("${OPENAI_API_KEY}") String openaiKey) {

        OpenAiChatModel groqModel = OpenAiChatModel.builder()
                .options(OpenAiChatOptions.builder()
                        .baseUrl("https://api.groq.com/openai/v1")
                        .apiKey(groqKey)
                        .model("openai/gpt-oss-120b")
                        .replayReasoningContent(false)
                        .build())
                .build();

        OpenAiChatModel openaiModel = OpenAiChatModel.builder()
                .options(OpenAiChatOptions.builder()
                        .apiKey(openaiKey)
                        .model("gpt-5-mini")
                        .build())
                .build();

        return new ClientiAi(ChatClient.create(groqModel), ChatClient.create(openaiModel));
    }
}

// Un record, non una Map<String, ChatClient>: Spring riempirebbe la Map con i bean di tipo ChatClient, non con questa
record ClientiAi(ChatClient groq, ChatClient openai) {}
```

`src/main/java/it/corso/ai/RispostaController.java`
```java
package it.corso.ai;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/ai")
class RispostaController {

    private final ClientiAi clienti;

    RispostaController(ClientiAi clienti) {
        this.clienti = clienti;
    }

    @GetMapping("/risposta")
    String risposta(@RequestParam String provider, @RequestParam String domanda) {
        // Scelta del client in base al parametro
        ChatClient client = switch (provider) {
            case "groq" -> clienti.groq();
            case "openai" -> clienti.openai();
            default -> throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Provider sconosciuto: " + provider);
        };
        return client.prompt().user(domanda).call().content();
    }
}
```

Prova:
```bash
curl "http://localhost:8080/ai/risposta?provider=groq&domanda=Che%20cos%27%C3%A8%20un%20token%3F"
curl "http://localhost:8080/ai/risposta?provider=openai&domanda=Che%20cos%27%C3%A8%20un%20token%3F"
curl -i "http://localhost:8080/ai/risposta?provider=gemini&domanda=Ciao"
```

Output atteso: le prime due chiamate restituiscono un breve testo sui token (diverso per i due provider e a ogni chiamata); la terza risponde `HTTP/1.1 400`.

- `OpenAiChatModel.builder().options(...)` costruisce un modello con indirizzo, chiave e modello propri, indipendente dalle proprietà `spring.ai.openai.*`.
- `ChatClient.create(modello)` crea un client senza passare dal `Builder` auto-configurato.
- Il `switch` sul parametro decide quale client usare; il resto della catena (`prompt().user().call().content()`) è identico.
- `ResponseStatusException` trasforma il provider sconosciuto in un errore HTTP 400, non in un 500 generico.
- **Cosa succede se** manca `GROQ_API_KEY` o `OPENAI_API_KEY`? L'applicazione non parte: il segnaposto `${...}` di `@Value` non può essere risolto. Servono entrambe, anche se poi usi un solo provider.
- **Cosa succede se** chiami `provider=GROQ` (maiuscolo)? Il `switch` è sensibile alle maiuscole: risposta 400. Per default Spring Boot non mostra il messaggio dell'errore nel corpo della risposta, solo lo status.
- **Cosa succede se** un provider supera i limiti o è offline? La chiamata lancia un'eccezione e l'endpoint risponde 500: un failover va scritto a mano (catturare l'errore e riprovare con l'altro client).

### Avanzato

RAG completo sul regolamento aziendale: embedding locale, `SimpleVectorStore`, caricamento di 5 documenti all'avvio, un endpoint che mostra la ricerca e uno che risponde con `QuestionAnswerAdvisor`. Dipende dal progetto della Parte 1 (profilo Groq per la chat), **senza** la classe `MultiProviderConfig` dell'Intermedio: richiede sempre entrambe le chiavi, qui non serve. Da verificare prima della lezione: i punteggi riportati sono indicativi; `getText()` e `getScore()` di `Document` vanno riconfermati in 2.0 (in 1.x il testo era `getContent()`); gli import di `Document` ed `EmbeddingModel` non rientrano nell'elenco verificato.

Dipendenze da **aggiungere** al `pom.xml` (versioni gestite dal BOM):
```xml
<dependency>
  <groupId>org.springframework.ai</groupId>
  <artifactId>spring-ai-vector-store</artifactId>
</dependency>
<dependency>
  <groupId>org.springframework.ai</groupId>
  <artifactId>spring-ai-vector-store-advisor</artifactId>
</dependency>
<dependency>
  <groupId>org.springframework.ai</groupId>
  <artifactId>spring-ai-starter-model-transformers</artifactId>
</dependency>
```

`src/main/resources/application.properties` (sostituisce quello degli esempi precedenti)
```properties
spring.ai.openai.api-key=${GROQ_API_KEY}
spring.ai.openai.base-url=https://api.groq.com/openai/v1
spring.ai.openai.chat.model=openai/gpt-oss-120b
spring.ai.openai.chat.replay-reasoning-content=false

# Groq non ha embeddings: si usa il modello locale (e si evitano due EmbeddingModel)
spring.ai.model.embedding=transformers
```

`src/main/java/it/corso/ai/RagConfig.java`
```java
package it.corso.ai;

import java.util.List;
import java.util.Map;

import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class RagConfig {

    // Store in memoria: usa l'EmbeddingModel locale per creare i vettori
    @Bean
    VectorStore vectorStore(EmbeddingModel embeddingModel) {
        return SimpleVectorStore.builder(embeddingModel).build();
    }

    // Ingestion: a ogni avvio carica i documenti (i testi sono già brevi: niente chunking)
    @Bean
    CommandLineRunner caricaRegolamento(VectorStore store) {
        return args -> store.add(List.of(
            new Document("Le ferie vanno richieste almeno 10 giorni prima tramite il portale HR.",
                    Map.of("argomento", "ferie")),
            new Document("I rimborsi spese si richiedono entro 30 giorni, allegando gli scontrini.",
                    Map.of("argomento", "rimborsi")),
            new Document("Il lavoro da remoto è consentito fino a 2 giorni a settimana, previo accordo con il responsabile.",
                    Map.of("argomento", "remoto")),
            new Document("La password scade ogni 90 giorni e deve avere almeno 12 caratteri.",
                    Map.of("argomento", "sicurezza")),
            new Document("Per l'assistenza IT si apre un ticket e si riceve risposta entro 4 ore lavorative.",
                    Map.of("argomento", "it"))));
    }
}
```

`src/main/java/it/corso/ai/RagController.java`
```java
package it.corso.ai;

import java.util.List;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/rag")
class RagController {

    private final VectorStore vectorStore;
    private final ChatClient chatClient;

    RagController(VectorStore vectorStore, ChatClient.Builder builder) {
        this.vectorStore = vectorStore;
        this.chatClient = builder
                .defaultSystem("Rispondi solo in base al contesto fornito. "
                        + "Se la risposta non è nel contesto, rispondi esattamente: Non lo so.")
                // L'advisor cerca nello store e aggiunge i testi trovati al prompt
                .defaultAdvisors(QuestionAnswerAdvisor.builder(vectorStore)
                        .searchRequest(SearchRequest.builder()
                                .similarityThreshold(0.4d)   // scarta i risultati poco simili
                                .topK(2)                     // al massimo 2 passaggi
                                .build())
                        .build())
                .build();
    }

    // Solo ricerca: nessuna chiamata al modello di chat
    @GetMapping("/cerca")
    List<Trovato> cerca(@RequestParam String q) {
        List<Document> trovati = vectorStore.similaritySearch(
                SearchRequest.builder().query(q).topK(3).build());
        return trovati.stream()
                .map(d -> new Trovato(d.getText(), d.getScore()))
                .toList();
    }

    // RAG completo: ricerca + prompt aumentato + risposta
    @GetMapping("/chiedi")
    String chiedi(@RequestParam String domanda) {
        return chatClient.prompt().user(domanda).call().content();
    }
}

record Trovato(String testo, Double punteggio) {}
```

Prova:
```bash
curl "http://localhost:8080/rag/cerca?q=Come%20chiedo%20le%20ferie%3F"
curl "http://localhost:8080/rag/chiedi?domanda=Ogni%20quanto%20scade%20la%20password%3F"
curl "http://localhost:8080/rag/chiedi?domanda=Chi%20ha%20vinto%20i%20mondiali%20del%202006%3F"
```

Output atteso (punteggi e testo indicativi, variano col modello).

Primo `curl` (`/rag/cerca`):
```json
[{"testo":"Le ferie vanno richieste almeno 10 giorni prima tramite il portale HR.","punteggio":0.62},
 {"testo":"Il lavoro da remoto è consentito fino a 2 giorni a settimana, previo accordo con il responsabile.","punteggio":0.31},
 {"testo":"I rimborsi spese si richiedono entro 30 giorni, allegando gli scontrini.","punteggio":0.27}]
```

Secondo e terzo `curl` (`/rag/chiedi`), una riga ciascuno:
```
La password scade ogni 90 giorni e deve avere almeno 12 caratteri.
Non lo so
```

- `SimpleVectorStore` usa l'`EmbeddingModel` per trasformare ogni documento in vettore; `/rag/cerca` mostra il risultato grezzo della ricerca, con il punteggio di similarità.
- `/rag/cerca` non imposta la soglia (default 0.0): restituisce sempre i primi 3, anche poco pertinenti. Serve a **vedere i punteggi** e calibrare.
- `QuestionAnswerAdvisor` fa da intermediario: cerca (topK 2, soglia 0.4), aggiunge i testi al prompt e solo allora chiama il modello.
- Il system prompt vincola il modello al contesto: è ciò che produce "Non lo so" per le domande fuori tema.
- **Cosa succede se** il testo è italiano e il modello di embedding è addestrato soprattutto su inglese? I punteggi risultano più bassi e l'ordine dei risultati meno affidabile. Con la soglia 0.4 un passaggio giusto potrebbe essere scartato: in tal caso abbassarla, o passare a un modello multilingua.
- **Cosa succede se** la soglia è troppo alta (es. 0.9)? Nessun passaggio la supera, il contesto è vuoto e quasi ogni domanda riceve "Non lo so". Troppo bassa (es. 0.0)? Passano anche testi irrilevanti e il modello può rispondere basandosi sul passaggio sbagliato.
- **Cosa succede se** la domanda è fuori contesto? Con una soglia adeguata il contesto è vuoto e il modello dovrebbe rispondere "Non lo so"; con una soglia bassa arrivano comunque passaggi non pertinenti, e il system prompt diventa l'unica difesa. Il comportamento esatto con contesto vuoto va verificato provando.
- **Cosa succede se** riavvii l'applicazione? Lo store in memoria si svuota; qui il `CommandLineRunner` ricarica i 5 documenti a ogni avvio. In produzione si usa uno store persistente e l'ingestion non si ripete a ogni avvio.

## Procedura: micro chat (tappa 10)

Prerequisito: la chiave Groq nel file `.env`, come nella procedura della Parte 1.

1. Avvia la chat con memoria e tool:
   ```bash
   ./mvnw spring-boot:run
   ```
2. Per attivare anche il RAG, avvia con i profili `rag` e `rag-multilingua`:
   ```bash
   ./mvnw spring-boot:run -Dspring-boot.run.profiles=groq,rag,rag-multilingua
   ```
   Al primo avvio il modello di embedding multilingua (circa 135 MB) viene scaricato: fallo prima della lezione, con una buona connessione.
3. Apri `http://localhost:8080/chat.html`. In alto compaiono modello, profili e lo stato del RAG (acceso o spento).
4. Segui lo scenario, nell'ordine indicato:
   - "Quanti MOUSE-01 abbiamo?": compare il chip `giacenza(...)` con il risultato, prima della risposta.
   - "Riserva 3 pezzi di MON-27": compare il chip del tool `riserva`.
   - "E ne restano quanti?": la risposta usa la memoria, senza ripetere il codice.
   - "Mi chiamo Anna", poi "Come mi chiamo?": la memoria ricorda il nome.
   - Premi *Nuova conversazione*, poi chiedi di nuovo "Come mi chiamo?": il modello non lo sa più.
   - Con il profilo `rag`: "Ogni quanto scade la password?": sotto la risposta compaiono le fonti dal regolamento.
5. Se la chat mostra un errore, leggi il messaggio. Chiave non valida, quota gratuita esaurita (errore 429) e modello non disponibile sono i casi più frequenti: la chat li spiega in italiano, senza stack trace.

Cosa osservare: il chip del tool compare prima della risposta, perché il modello chiede il dato e l'applicazione lo esegue. La memoria salva solo la domanda e la risposta finale, non i messaggi intermedi dei tool.

## Esercizi

### Facile
**Obiettivo:** aggiungere un terzo profilo, `groq-veloce`, che usa Groq con un modello più piccolo e veloce.
**Dati:** il progetto dell'esempio Base; modello `openai/gpt-oss-20b`; stessa chiave e stesso indirizzo del profilo `groq`. Con `-Dspring-boot.run.profiles=groq-veloce` il profilo `groq` **non** resta attivo: il nuovo file deve contenere tutte le proprietà, compresa `spring.ai.openai.chat.replay-reasoning-content=false`.
**Risultato atteso:** avviando con il profilo `groq-veloce` (da riga di comando, senza modificare `application.properties`), `GET /ai/provider` restituisce `{"profilo":"groq-veloce","modello":"openai/gpt-oss-20b"}`. Avviando con `groq` o `openai` i risultati restano quelli dell'esempio.

### Medio
**Obiettivo:** creare l'endpoint `GET /ai/confronta?domanda=...` che interroga **entrambi** i provider e confronta risposte e tempi.
**Dati:** il progetto dell'esempio Intermedio (record `ClientiAi`); per la durata usa un cronometro Java a tua scelta, in millisecondi, attorno a ciascuna chiamata. Domanda di prova: `Spiega in una frase cos'è un token`.
**Risultato atteso:** un JSON con una voce per provider, ad esempio `{"groq":{"risposta":"...","durataMs":412},"openai":{"risposta":"...","durataMs":1630}}`. Se la chiamata a un provider fallisce, quella voce contiene un campo `errore` con un messaggio leggibile al posto di `risposta`, e l'altro provider risponde comunque; l'endpoint non restituisce mai 500. Per provare il caso d'errore imposta una chiave **non valida** (es. `OPENAI_API_KEY=finta`): il provider la rifiuta e la voce contiene `errore`. Una chiave **assente** non è simulabile: senza la variabile l'applicazione non si avvia (vedi Intermedio).

### Difficile
**Obiettivo:** realizzare le FAQ di una biblioteca con il RAG e un endpoint `GET /biblioteca/chiedi?domanda=...` che restituisce le fonti usate.
**Dati:** il progetto dell'esempio Avanzato, con i 5 documenti del regolamento sostituiti da questi 6:
- Orari: lunedì-venerdì 9-19, sabato 9-13.
- La tessera permette il prestito di 5 libri per 30 giorni.
- Il prestito si rinnova una sola volta, se nessuno ha prenotato il libro.
- Il ritardo costa 0,50 euro al giorno per ogni libro.
- La tessera è gratuita e si fa in sede con un documento d'identità.
- La sala studio ha 40 posti e si prenota dal sito fino a 2 giorni prima.

**Risultato atteso:** la risposta è un JSON `{"risposta":"...","fonti":["...","..."]}`, dove `fonti` contiene i testi dei documenti che superano la stessa soglia con lo stesso topK usati per la risposta. Tre domande di prova:
- `Fino a quando posso tenere un libro?` → 30 giorni, rinnovabile una sola volta (fonti: prestito e rinnovo);
- `Quanto pago se restituisco in ritardo?` → 0,50 euro al giorno per libro (fonte: ritardo);
- `Avete il servizio di fotocopie?` → `Non lo so`, con l'elenco delle fonti vuoto.

Scegli topK e soglia provando: annota i punteggi che ottieni per le tre domande e giustifica in due righe i valori scelti.

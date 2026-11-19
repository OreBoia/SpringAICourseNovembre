# Corso Spring AI — progetto a tappe

Progetto didattico **Spring Boot 4.1.2 + Spring AI 2.0.2** (Java 25) che accompagna le tre parti delle slide:

| Parte | File delle slide | Tappe |
|---|---|---|
| 1 — Fondamenti e ChatClient | `slide-spring-ai-1-fondamenti-e-chatclient.md` | 1, 2, 3 |
| 2 — Structured Output, Tools, Memory | `slide-spring-ai-2-structured-output-tools-memory.md` | 4, 5, 6 |
| 3 — Multi-provider e RAG | `slide-spring-ai-3-multiprovider-e-rag.md` | 7, 8, 9 |
| Finale — tutto insieme | tutte e tre | **10 · micro chat** |

Un solo progetto, un package per tappa (`it.corso.ai.tappaNN_...`). Ogni package ha un `package-info.java` con
la sezione delle slide di riferimento, i concetti, i comandi di prova e "cosa osservare".

---

## Avvio rapido

Prerequisiti: **JDK 25**. Maven non serve: c'è il wrapper `./mvnw` (`mvnw.cmd` su Windows).

**1. Chiave Groq (gratuita, una volta sola).** Su <https://console.groq.com/keys> → *Create API Key*. Poi,
nella cartella `corso-spring-ai/`:

```bash
cp .env.example .env      # e nel file .env incolla la chiave: GROQ_API_KEY=gsk_...
```

Il file `.env` è letto all'avvio (`spring.config.import` in `application.properties`) ed è in `.gitignore`:
la chiave non finisce mai su Git. In alternativa vale ancora `export GROQ_API_KEY=...`.
Avviare sempre dalla cartella `corso-spring-ai/`: il percorso di `.env` è relativo alla cartella di lavoro.

**2. Avvio.**

```bash
./mvnw spring-boot:run
```

Poi apri <http://localhost:8080/> (le tappe) o direttamente <http://localhost:8080/chat.html> (la micro chat),
oppure usa i `curl` qui sotto.

I test girano **senza chiave** (usano un modello finto):

```bash
./mvnw test
```

---

## Profili

Le tappe convivono nello stesso progetto grazie ai profili Spring (che sono anche l'argomento della tappa 7).

| Profilo | Cosa attiva | Chiavi |
|---|---|---|
| `groq` *(default)* | Groq, `openai/gpt-oss-120b` | `GROQ_API_KEY` |
| `openai` | OpenAI, `gpt-5-mini` | `OPENAI_API_KEY` |
| `multi` | tappa 8: due provider scelti per richiesta | entrambe |
| `rag` | tappa 9: embedding locale, VectorStore, `/rag/**` | quella del provider |
| `rag-multilingua` | da aggiungere a `rag`: modello di embedding che conosce l'italiano | — |
| `soluzioni` | sostituisce gli stub degli esercizi con le soluzioni | — |

I profili si combinano con la virgola:

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=openai
./mvnw spring-boot:run -Dspring-boot.run.profiles=groq,rag
./mvnw spring-boot:run -Dspring-boot.run.profiles=groq,rag,rag-multilingua
./mvnw spring-boot:run -Dspring-boot.run.profiles=groq,multi          # servono GROQ_API_KEY e OPENAI_API_KEY
./mvnw spring-boot:run -Dspring-boot.run.profiles=groq,soluzioni
```

Il profilo `rag` scarica al primo avvio il modello di embedding (~80 MB, oppure ~135 MB con `rag-multilingua`):
fatelo prima della lezione, con una buona connessione.

---

## Il percorso in aula

### Parte 1 — Fondamenti e ChatClient

**Tappa 1 · Il primo ChatClient** (`tappa01_chat`): `prompt().user().call().content()`.
```bash
curl "http://localhost:8080/ai/chat?message=Spiega%20in%20due%20righe%20cos%27%C3%A8%20un%20LLM"
```

**Tappa 2 · System prompt, template, token** (`tappa02_tutor`).
```bash
curl "http://localhost:8080/ai/ripasso?argomento=le%20interfacce"
# demo teoria: stessa domanda N volte → risposte diverse (LLM non deterministico) e costo in token
curl "http://localhost:8080/demo/non-determinismo?domanda=Inventa%20un%20nome%20per%20un%20gatto&n=3"
```

**Tappa 3 · Streaming SSE** (`tappa03_streaming`).
```bash
curl -N "http://localhost:8080/ai/storia?tema=un%20robot%20bibliotecario"
```

### Parte 2 — Structured Output, Tools, Memory, Advisors

**Tappa 4 · Structured output** (`tappa04_structured`).
```bash
curl "http://localhost:8080/ai/ricetta?piatto=spaghetti%20aglio%20olio"
curl "http://localhost:8080/ai/ricette?ingrediente=zucchine"
# demo teoria (NLP): sentimento e temi di recensioni d'hotel, con enum e gestione dell'errore
curl -X POST -H "Content-Type: application/json" \
  -d '["Camera pulitissima, colazione ottima!","Rumore tutta la notte, personale scortese.","Posizione comoda, niente di speciale."]' \
  http://localhost:8080/demo/recensioni
```

**Tappa 5 · Tools** (`tappa05_tools`). Guardare la console: le righe `[tool] ...` provano la chiamata.
Il magazzino è in memoria: riavviare tra una serie di prove e l'altra.
```bash
curl "http://localhost:8080/ai/magazzino?domanda=Quanti%20MOUSE-01%20abbiamo%3F"
curl "http://localhost:8080/ai/magazzino?domanda=Riserva%203%20pezzi%20di%20MON-27"
curl "http://localhost:8080/ai/magazzino?domanda=Quanti%20MON-99%20abbiamo%3F"
```

**Tappa 6 · Memory e Advisors** (`tappa06_memory`). Prima senza memoria (il modello è stateless), poi con.
```bash
curl -X POST -H "Content-Type: text/plain" -d "Ciao, sono Anna" http://localhost:8080/demo/senza-memoria
curl -X POST -H "Content-Type: text/plain" -d "Come mi chiamo?" http://localhost:8080/demo/senza-memoria

curl -X POST -H "Content-Type: text/plain" -d "Ciao, sono Anna. Quanti MON-27 abbiamo?" http://localhost:8080/ai/assistente/anna
curl -X POST -H "Content-Type: text/plain" -d "Riservane 3 per me" http://localhost:8080/ai/assistente/anna
curl -X POST -H "Content-Type: text/plain" -d "Come mi chiamo?" http://localhost:8080/ai/assistente/anna
curl -X POST -H "Content-Type: text/plain" -d "Come mi chiamo?" http://localhost:8080/ai/assistente/luca
```
Nei log (livello DEBUG) `SimpleLoggerAdvisor` mostra la storia aggiunta al prompt.

### Parte 3 — Multi-provider e RAG

**Tappa 7 · Profiles** (`tappa07_profiles`). Stesso codice, provider diverso a seconda del profilo.
```bash
curl http://localhost:8080/ai/provider        # {"profilo":"groq","modello":"openai/gpt-oss-120b"}
```

**Tappa 8 · Switch a runtime** (`tappa08_multiprovider`, profilo `multi`).
```bash
curl "http://localhost:8080/ai/risposta?provider=groq&domanda=Che%20cos%27%C3%A8%20un%20token%3F"
curl "http://localhost:8080/ai/risposta?provider=openai&domanda=Che%20cos%27%C3%A8%20un%20token%3F"
curl -i "http://localhost:8080/ai/risposta?provider=gemini&domanda=Ciao"     # 400
```

**Tappa 9 · RAG** (`tappa09_rag`, profilo `rag`). Ordine consigliato: embedding → ricerca → RAG.
```bash
# 1. embedding e similarità del coseno (demo teoria)
curl "http://localhost:8080/rag/similarita?a=Quando%20posso%20andare%20in%20ferie%3F&b=Come%20chiedo%20giorni%20di%20vacanza%3F"
# 2. ricerca nel VectorStore, con punteggi (nessuna chiamata al modello di chat)
curl "http://localhost:8080/rag/cerca?q=Come%20chiedo%20le%20ferie%3F"
# 3. RAG completo con QuestionAnswerAdvisor
curl "http://localhost:8080/rag/chiedi?domanda=Ogni%20quanto%20scade%20la%20password%3F"
curl "http://localhost:8080/rag/chiedi?domanda=Chi%20ha%20vinto%20i%20mondiali%20del%202006%3F"
```

### Finale — Tappa 10 · Micro chat (`tappa10_microchat`)

Una chat vera, in una pagina: <http://localhost:8080/chat.html>. Mette insieme tutto il corso:

| Cosa si vede | Da quale tappa | Come funziona |
|---|---|---|
| la risposta compare a pezzi | 3 · streaming | `stream().chatClientResponse()` → eventi SSE |
| ricorda la conversazione, anche dopo il refresh | 6 · memoria | `MessageChatMemoryAdvisor`; l'ID è la **sessione HTTP**, deciso dal server |
| chip `⚙ giacenza(MOUSE-01) → 25` sopra la risposta | 5 · tools | i `ToolCallback` di `MagazzinoTools` avvolti da un decoratore che avvisa la pagina |
| "Fonti dal regolamento" sotto la risposta | 9 · RAG | `QuestionAnswerAdvisor`, attivo solo con il profilo `rag` |
| "Nuova conversazione", contatore N/20 | 6 · finestra | `ChatMemory.clear(id)`, `MessageWindowChatMemory` |
| errori leggibili (chiave, quota, modello) | 3 · errori nello stream | eccezione → evento `errore` |

```bash
./mvnw spring-boot:run                                                       # chat + memoria + tools
./mvnw spring-boot:run -Dspring-boot.run.profiles=groq,rag,rag-multilingua   # anche il RAG
```

**Scenario per la classe:** "Quanti MOUSE-01 abbiamo?" (chip del tool) → "Riserva 3 pezzi di MON-27" →
"E ne restano quanti?" (memoria + tool) → "Mi chiamo Anna" → "Come mi chiamo?" → *Nuova conversazione* →
"Come mi chiamo?" (dimenticato). Con il RAG: "Ogni quanto scade la password?" (fonti).

**Ordine degli advisor** (il punto più istruttivo della tappa, verificato dai test):
memoria (MIN+200) → RAG (MIN+250, impostato a mano) → ciclo dei tool (MIN+300) → modello.

- La memoria avvolge tutto: la storia entra nel prompt **una volta** e in memoria finiscono solo domanda e
  risposta finale, mai i messaggi intermedi dei tool (Groq rifiuterebbe una storia con tool call senza risposta).
- Il `QuestionAnswerAdvisor` ha ordine 0 di default: starebbe *dentro* il ciclo dei tool e cercherebbe a ogni giro.
  Spostato prima, cerca una volta per domanda e la memoria salva la domanda originale, senza il contesto.
- Se il provider fallisce, in memoria resta la domanda senza risposta (la memoria la salva prima di chiamare il modello).

Endpoint, utili anche con `curl` (`-c/-b cookie.txt` per tenere la sessione):
`POST /chat/messaggio` (text/plain → SSE con eventi `strumento`, `fonti`, `testo`, `errore`, `fine`),
`GET /chat/stato`, `GET /chat/storia`, `POST /chat/nuova`.

```bash
curl -N -c cookie.txt -b cookie.txt -X POST -H "Content-Type: text/plain" \
  -d "Quanti MOUSE-01 abbiamo?" http://localhost:8080/chat/messaggio
```

---

## Calibrazione del RAG: punteggi misurati

Punteggi di similarità ottenuti eseguendo davvero il progetto (embedding locale, nessuna chiave necessaria).
Sono un ottimo materiale per la slide "Come calibrare soglia e topK".

**Regolamento aziendale (`/rag/cerca`, primo risultato)**

| Domanda | Modello predefinito (`rag`) | Multilingua (`rag-multilingua`) |
|---|---|---|
| Come chiedo le ferie? | 0.565 ferie ✔ | 0.520 ferie ✔ |
| Ogni quanto scade la password? | 0.763 password ✔ | 0.786 password ✔ |
| Posso lavorare da casa? | **0.473 ferie ✘** (remoto 0.452) | 0.363 remoto ✔ |
| Entro quando chiedo un rimborso? | 0.555 rimborsi ✔ | 0.448 rimborsi ✔ |
| Chi ha vinto i mondiali del 2006? *(fuori tema)* | **0.496** | 0.039 |

**Similarità tra frasi (`/rag/similarita`)**, "Quando posso andare in ferie?" contro:

| Frase | Predefinito | Multilingua |
|---|---|---|
| Come chiedo giorni di vacanza? | 0.399 | 0.778 |
| La password scade ogni 90 giorni | 0.317 | 0.156 |

Cosa mostrano i numeri:
- con il modello predefinito (addestrato quasi solo su inglese) la domanda fuori tema ottiene **0.496**,
  più di alcune domande pertinenti: la soglia 0.4 delle slide la lascia passare, e "Non lo so" dipende
  solo dal system prompt. È esattamente il caso "i due gruppi si sovrappongono" delle slide;
- il modello multilingua separa bene i gruppi: è la soluzione "si cambia modello" delle slide.

**Esercizio Difficile della Parte 3 (biblioteca):** con il modello predefinito non esiste una soglia corretta
("fotocopie", fuori tema, 0.494 contro "ritardo", pertinente, 0.511). Con `rag-multilingua` funzionano
**topK 2 e soglia 0.25** (prestito 0.46 e rinnovo 0.42; ritardo 0.29; fotocopie al massimo 0.21).
La soluzione va quindi avviata con `-Dspring-boot.run.profiles=groq,rag,rag-multilingua,soluzioni`.

---

## Esercizi

Gli esercizi delle slide (Facile/Medio/Difficile per ogni parte) sono in `it.corso.ai.esercizi.parteN`:
ogni classe ha l'endpoint già mappato, il testo dell'esercizio nel Javadoc e un `TODO`.
Finché non viene completato, l'endpoint risponde **HTTP 501**.

| Esercizio | Endpoint | Profilo |
|---|---|---|
| P1 Facile | `GET /ai/traduci?testo=&lingua=` | — |
| P1 Medio | `GET /ai/commit?messaggio=` | — |
| P1 Difficile | `POST /ai/riassunto` (SSE) | — |
| P2 Facile | `POST /ai/libro` | — |
| P2 Medio | `GET /ai/sconto?domanda=` | — |
| P2 Difficile | `POST /ai/chat/{utente}` | — |
| P3 Facile | nuovo file `application-groq-veloce.properties` | — |
| P3 Medio | `GET /ai/confronta?domanda=` | `multi` |
| P3 Difficile | `GET /biblioteca/chiedi?domanda=` | `rag` |

Le **soluzioni** sono in `it.corso.ai.soluzioni` e si attivano con il profilo `soluzioni`
(la P3 Facile è in `docs/soluzioni/application-groq-veloce.properties`). Le soluzioni di P1 e P2 hanno test
in `src/test/java/it/corso/ai/soluzioni`.

---

## Test senza chiave API

`src/test/java/it/corso/ai/FakeChatModel.java` implementa `ChatModel` con risposte decise dal test
e registra i prompt ricevuti. Si possono mostrare in aula:

| Test | Cosa dimostra |
|---|---|
| `RipassoControllerTest` | il template sostituisce `{argomento}`, il system prompt arriva al modello, i token si leggono dai metadati |
| `StoriaControllerTest` | lo stream arriva a pezzi; un errore del provider diventa un ultimo pezzo leggibile |
| `StructuredOutputTest` | JSON → record e liste; Spring AI aggiunge lo schema al prompt; JSON malformato → eccezione / 502 |
| `MagazzinoToolsTest` | un tool è un metodo Java: si testa senza modello; validazione degli argomenti |
| `MemoriaTest` | la memoria rimanda la storia al modello; sessioni separate; senza `CONVERSATION_ID` → `IllegalArgumentException` |
| `SimilaritaTest` | similarità del coseno su vettori calcolabili alla lavagna |
| `MicroChatTest` | il ciclo dei tool (il modello finto *chiede* `giacenza`, l'app la esegue), la memoria senza messaggi di tool, l'ordine RAG/memoria, le fonti, gli errori di Groq tradotti |
| `CorsoAiApplicationTests` | l'app intera parte con tutte le tappe; i profili accendono/spengono le parti giuste; la micro chat via HTTP con sessione |

---

## Differenze rispetto alle slide

I punti "da verificare" delle slide sono stati controllati compilando ed eseguendo il progetto.

**Confermati:** pacchetti di `ChatMemory`, `SimpleLoggerAdvisor` (con costruttore senza argomenti),
`QuestionAnswerAdvisor`, `Document.getText()` / `getScore()`, `OpenAiChatOptions.builder().baseUrl().apiKey().replayReasoningContent()`;
senza `CONVERSATION_ID` la memoria lancia `IllegalArgumentException`; una `ChatMemory` creata nel costruttore
(esercizio P2 Difficile) convive con quella auto-configurata.

**Da aggiornare nelle slide:**
- **Modelli Groq:** `llama-3.3-70b-versatile` e `llama-3.1-8b-instant` sono stati dismessi da Groq il 16 agosto 2026 (pagina delle deprecazioni: `console.groq.com/docs/deprecations`). Il progetto usa `openai/gpt-oss-120b` (principale) e `openai/gpt-oss-20b` (profilo leggero), i sostituti indicati da Groq. Il supporto ai tool di questi modelli va ancora verificato con una chiave reale.
- **Streaming nel browser (Parte 1, Avanzato):** Spring scrive `data:` senza spazio e lo standard SSE toglie uno spazio
  iniziale: con `EventSource` un token come `" volta"` arriva come `"volta"` e le parole si attaccano. Con `curl` non si vede.
  Le pagine del progetto leggono lo stream con `fetch` (`static/sse.js`) e la micro chat manda il testo dentro un JSON.
- **Versione di Java:** il progetto usa **Java 25** (`<java.version>25</java.version>`); le slide (Parte 1, "Spring AI: che cos'è") indicano JDK 21. Spring Boot 4.1 e Spring AI 2.0 supportano entrambe. Nel codice compare la variabile senza nome `_` per i parametri lambda non usati (es. `onErrorResume(_ -> ...)`), standard da Java 22.
- **Versione Spring AI:** qui 2.0.2 (ultima della linea 2.0.x); le slide citano 2.0.1. Spring Initializr per Boot 4.1.2 propone ancora 2.0.1: funzionano entrambe.
- **Messaggio d'errore nel corpo:** `application.properties` imposta `spring.web.error.include-message=always` (in Boot 4 sostituisce `server.error.include-message`),
  così il 400 di `/ai/risposta` e il 501 degli esercizi mostrano il motivo. Le slide (Parte 3, Intermedio) dicono che di default non si vede: senza quella riga è così.
- **Punteggi RAG:** i valori di esempio della Parte 3 (0.62 / 0.31 / 0.27) sono diversi da quelli misurati (0.565 / 0.408 / 0.372);
  la soglia 0.4 non esclude la domanda sui mondiali con il modello predefinito (vedi "Calibrazione").
- **`spring.ai.model.embedding=none`** in `application.properties`: senza il profilo `rag` nessun modello di embedding viene creato
  (le slide lo impostano a `transformers` sempre, perché lì il RAG è un progetto a parte).
- I record di risposta `InfoProvider` e `Trovato` sono annidati nei controller invece che top-level (evita conflitti tra tappe).

**Non verificato qui** (serve una chiave valida): il contenuto delle risposte dei modelli, il supporto ai tool
dei modelli Groq indicati, i tempi di risposta. Da provare prima della lezione.

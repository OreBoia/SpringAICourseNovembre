# Singleton Pattern

## Spiegazione teorica

- Il pattern garantisce che di una classe esista **una sola istanza**, accessibile da tutto il programma.
- In Java puro si ottiene con un costruttore privato e un campo statico che contiene l'istanza.
- Il modo più semplice e sicuro è l'`enum` con un solo valore: la JVM garantisce l'unicità anche con reflection e serializzazione.
- In Spring il singleton è lo **scope predefinito** dei bean. Esiste una sola istanza **per container**, non per JVM.
- Attenzione: un singleton è condiviso da tutti. Se ha campi modificabili, serve sincronizzazione.

### Parametri

**Java puro** (nessuna annotazione)

| Elemento | Ruolo |
|---|---|
| `private` costruttore | Impedisce `new` dall'esterno |
| `static` campo `INSTANCE` | Contiene l'unica istanza |
| `getInstance()` | Restituisce l'istanza condivisa |

**Spring** (`@Scope`, insieme a `@Component` o `@Bean`)

| Annotazione | Attributo | Valori |
|---|---|---|
| `@Scope` | `value` | `"singleton"` (default), `"prototype"`, `"request"`, `"session"` |
| `@Scope` | `proxyMode` | `DEFAULT`, `NO`, `TARGET_CLASS` (serve per iniettare uno scope più corto in un singleton) |

## Esempi teorici di utilizzo

- **Configurazione dell'applicazione:** le impostazioni si leggono una volta e poi servono a tutto il programma.
- **Registro (log) condiviso:** tutte le parti del programma scrivono sullo stesso registro.
- **Pool di connessioni al database:** aprire una connessione costa molto, quindi se ne tiene una raccolta condivisa.
- **Cache in memoria:** i risultati già calcolati si conservano in un solo punto, visibile a tutti.
- **Non adatto a:** dati che cambiano per ogni utente, come il carrello di un cliente. Lì serve uno scope diverso.

## Esempi di codice

### Base

Cosa mostra: singleton classico in Java puro, con costruttore privato.

```java
class Configurazione {
    private static final Configurazione INSTANCE = new Configurazione();  // creata una sola volta

    private String urlServer = "http://localhost:8080";

    private Configurazione() { }                 // costruttore privato: nessuno può fare new

    static Configurazione getInstance() {
        return INSTANCE;                         // sempre lo stesso oggetto
    }

    String getUrlServer() { return urlServer; }
}

public class Main {
    public static void main(String[] args) {
        Configurazione a = Configurazione.getInstance();
        Configurazione b = Configurazione.getInstance();
        System.out.println(a == b);              // true: stesso oggetto
        System.out.println(a.getUrlServer());    // http://localhost:8080
    }
}
```

- Il costruttore `private` rende impossibile creare un'istanza dall'esterno.
- Il campo `static` viene creato una sola volta, al caricamento della classe.
- `==` confronta i riferimenti: `true` significa che sono lo stesso oggetto.

### Intermedio

Cosa mostra: singleton con `enum`, il modo consigliato in Java. Lo stato è protetto con `synchronized`.

```java
import java.util.ArrayList;
import java.util.List;

public enum Registro {
    INSTANCE;                                    // un solo valore = una sola istanza

    private final List<String> messaggi = new ArrayList<>();

    public synchronized void scrivi(String m) {  // un thread alla volta modifica la lista
        messaggi.add(m);
    }

    public synchronized List<String> leggi() {
        return List.copyOf(messaggi);            // copia immutabile: il chiamante non può modificare
    }
}

public class Main {
    public static void main(String[] args) {
        Registro.INSTANCE.scrivi("avvio");
        Registro.INSTANCE.scrivi("login utente");
        System.out.println(Registro.INSTANCE.leggi());   // [avvio, login utente]
    }
}
```

- Con `enum` la JVM garantisce una sola istanza, anche con serializzazione e reflection.
- Il costruttore non si può chiamare: non serve scriverlo.
- Lo stato resta modificabile, quindi va protetto con `synchronized` o con strutture thread-safe.

### Avanzato

Cosa mostra: scope `singleton` contro `prototype` in Spring, con la verifica dell'identità degli oggetti. (Import omessi.)

```java
@Component                                       // singleton: una sola istanza nel container
class ContatoreVisite {
    private int visite = 0;                      // ⚠️ stato condiviso da tutte le richieste

    synchronized int registra() {                // sincronizzato: il singleton è usato da più thread
        return ++visite;
    }
}

@Component
@Scope("prototype")                              // nuova istanza a ogni richiesta
class Biglietto { }

@SpringBootApplication
public class App {
    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }

    @Bean
    CommandLineRunner verifica(ApplicationContext ctx) {
        return args -> {
            // stesso oggetto due volte: singleton
            System.out.println(ctx.getBean(ContatoreVisite.class) == ctx.getBean(ContatoreVisite.class)); // true

            // oggetti diversi: prototype
            System.out.println(ctx.getBean(Biglietto.class) == ctx.getBean(Biglietto.class));             // false
        };
    }
}
```

- Il singleton è condiviso da tutto il container: lo stato va protetto.
- Il prototype crea un oggetto nuovo a ogni `getBean` o iniezione.
- Il confronto `==` mostra la differenza tra i due scope senza leggere il codice interno.

## Esercizi

### Facile

**Obiettivo:** creare un singleton con costruttore privato e verificare che sia unico.

**Consegna:** scrivi una classe `Termostato` con un campo `double temperatura` (valore iniziale 20.0) e i metodi `getTemperatura()` e `setTemperatura(double)`. Il costruttore deve essere privato e l'accesso passa solo da `getInstance()`. Dimostra che due riferimenti ottenuti con `getInstance()` vedono lo stesso valore dopo un `setTemperatura(22.5)`.

**Soluzione:**

```java
class Termostato {
    private static final Termostato INSTANCE = new Termostato();  // unica istanza

    private double temperatura = 20.0;

    private Termostato() { }                                      // nessuno può fare new

    static Termostato getInstance() {
        return INSTANCE;
    }

    double getTemperatura() { return temperatura; }
    void setTemperatura(double t) { temperatura = t; }
}

public class Main {
    public static void main(String[] args) {
        Termostato a = Termostato.getInstance();
        Termostato b = Termostato.getInstance();

        System.out.println(a.getTemperatura());  // 20.0
        a.setTemperatura(22.5);                  // modifico tramite a...
        System.out.println(b.getTemperatura());  // ...e b vede 22.5
        System.out.println(a == b);              // true: stesso oggetto
    }
}
```

Output:

```text
20.0
22.5
true
```

- Il costruttore `private` impedisce di creare altri `Termostato` dall'esterno.
- `a` e `b` puntano allo stesso oggetto, quindi la modifica fatta con `a` è visibile anche con `b`.

### Medio

**Obiettivo:** creare un singleton con `enum` che gestisce un credito e proteggerlo dall'accesso concorrente.

**Consegna:** un distributore di bevande ha un unico credito in euro. Scrivi un `enum Distributore` con `INSTANCE`, i metodi `inserisci(double)` e `eroga(double prezzo)`. `eroga` deve restituire `true` e scalare il credito solo se il credito basta, altrimenti `false`. Rendi i metodi `synchronized` e verifica il risultato con tre chiamate: inserisci 5 euro, eroga 2 euro, eroga 4 euro.

**Soluzione:**

```java
enum Distributore {
    INSTANCE;

    private double credito = 0.0;

    public synchronized void inserisci(double importo) {
        credito += importo;
    }

    public synchronized boolean eroga(double prezzo) {
        if (credito >= prezzo) {          // il credito basta
            credito -= prezzo;
            return true;
        }
        return false;                     // credito insufficiente: nessuna modifica
    }

    public synchronized double getCredito() {
        return credito;
    }
}

public class Main {
    public static void main(String[] args) {
        Distributore.INSTANCE.inserisci(5);
        System.out.println(Distributore.INSTANCE.eroga(2));          // true: credito 5 → 3
        System.out.println(Distributore.INSTANCE.eroga(4));          // false: 3 non basta
        System.out.println(Distributore.INSTANCE.getCredito());      // 3.0
    }
}
```

Output:

```text
true
false
3.0
```

- `eroga` controlla il credito e lo scala nello stesso blocco `synchronized`: due thread non possono spendere lo stesso credito due volte.
- Il secondo `eroga(4)` restituisce `false` e il credito resta invariato.

### Difficile

**Obiettivo:** usare la lazy initialization con l'idioma *holder*, che crea l'istanza solo quando serve.

**Consegna:** una stampante ha un lettore di impostazioni che legge un file pesante. Scrivi una classe `LettoreImpostazioni` con una classe interna statica `Holder` che contiene `static final LettoreImpostazioni INSTANCE = new LettoreImpostazioni();`. Aggiungi un messaggio nel costruttore, del tipo `"Lettura file impostazioni..."`. Dimostra che il messaggio compare una sola volta, alla prima chiamata di `getInstance()`, e non alla caricamento della classe esterna.

**Soluzione:**

```java
class LettoreImpostazioni {

    private LettoreImpostazioni() {
        System.out.println("Lettura file impostazioni...");   // eseguito solo alla creazione
    }

    private static class Holder {
        static final LettoreImpostazioni INSTANCE = new LettoreImpostazioni();
    }

    public static LettoreImpostazioni getInstance() {
        return Holder.INSTANCE;           // Holder viene caricata solo qui
    }
}

public class Main {
    public static void main(String[] args) {
        System.out.println("Programma avviato");          // il lettore non è ancora stato creato

        LettoreImpostazioni a = LettoreImpostazioni.getInstance();  // qui parte la lettura
        LettoreImpostazioni b = LettoreImpostazioni.getInstance();  // nessun nuovo messaggio

        System.out.println(a == b);                       // true
    }
}
```

Output:

```text
Programma avviato
Lettura file impostazioni...
true
```

- La classe interna `Holder` non viene caricata quando si carica `LettoreImpostazioni`: viene caricata alla prima chiamata di `getInstance()`.
- Il messaggio compare una sola volta, perché la JVM crea `INSTANCE` una sola volta e la riusa nelle chiamate successive.
- La JVM garantisce anche la sicurezza nei thread durante l'inizializzazione della classe `Holder`, senza `synchronized`.

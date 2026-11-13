# Strategy Pattern

## Spiegazione teorica

- Il pattern definisce una **famiglia di algoritmi**: ognuno sta in una classe propria, dietro la stessa interfaccia.
- Il codice che li usa conosce solo l'interfaccia. La strategia viene scelta dall'esterno e può cambiare senza toccare il resto.
- Vantaggi: niente catene di `if` o `switch` che crescono nel tempo. Una nuova strategia è una nuova classe (principio *open/closed*).
- In Spring ogni strategia è un bean (`@Component`). Il servizio riceve una `List` di interfaccia e sceglie quella giusta.

### Parametri

**Annotazioni** (per le strategie in Spring)

| Annotazione | Attributo | Significato |
|---|---|---|
| `@Component` | `value` | Nome del bean (default: nome della classe con iniziale minuscola) |
| `@Primary` | nessuno | Strategia predefinita quando ne viene richiesta una generica |
| `@Qualifier` | `value` | Sceglie il bean per nome durante l'iniezione |
| `@Order` | `value` | Ordine degli elementi quando si inietta una `List` (numeri bassi prima) |

**Interfaccia della strategia**: ha un solo metodo astratto, quindi può essere implementata anche con una lambda. L'annotazione `@FunctionalInterface` è facoltativa ma consigliata.

## Esempi teorici di utilizzo

- **Spese di spedizione:** standard, express o ritiro in negozio. Cambia solo il calcolo, non il flusso dell'ordine.
- **Metodo di pagamento:** carta, PayPal o bonifico, scelto dal cliente al momento dell'acquisto.
- **Sconti:** studente, over 65 o nessuno. Ogni regola diventa una classe separata.
- **Provider di intelligenza artificiale:** Groq o OpenAI, scelto dalla configurazione senza cambiare il codice che chiama il modello.
- **Compressione di file:** ZIP o GZIP, scelti a runtime dall'utente.

## Esempi di codice

### Base

Cosa mostra: un'interfaccia con due implementazioni, scelte dall'esterno.

```java
interface Sconto {
    double applica(double prezzo);               // ogni strategia implementa questo metodo
}

class ScontoStudente implements Sconto {
    public double applica(double prezzo) { return prezzo * 0.8; }   // 20% di sconto
}

class NessunoSconto implements Sconto {
    public double applica(double prezzo) { return prezzo; }
}

public class Negozio {
    public static void main(String[] args) {
        Sconto strategia = new ScontoStudente();     // scelta esterna della strategia
        System.out.println(strategia.applica(100));  // 80.0

        strategia = new NessunoSconto();             // cambio strategia: il resto non cambia
        System.out.println(strategia.applica(100));  // 100.0
    }
}
```

- `Sconto` è il contratto comune: `Negozio` non sa quale regola sta usando.
- Cambiare strategia significa cambiare il `new`, non il codice che chiama `applica`.
- Se la strategia ha un solo metodo, si può scrivere come lambda: `Sconto s = p -> p * 0.9;`.

### Intermedio

Cosa mostra: strategie come bean Spring. Il servizio le riceve tutte in una lista e le indicizza per nome. (Import omessi.)

```java
public interface Spedizione {
    String tipo();                               // chiave con cui si cerca la strategia
    double costo(double peso);
}

@Component
class Standard implements Spedizione {
    public String tipo() { return "standard"; }
    public double costo(double peso) { return 5 + peso * 0.5; }
}

@Component
class Express implements Spedizione {
    public String tipo() { return "express"; }
    public double costo(double peso) { return 12 + peso * 1.0; }
}

@Service
class CalcoloSpedizione {
    private final Map<String, Spedizione> strategie = new HashMap<>();

    // Spring inietta TUTTE le implementazioni di Spedizione in una lista
    CalcoloSpedizione(List<Spedizione> lista) {
        for (Spedizione s : lista) {
            strategie.put(s.tipo(), s);          // indicizza ogni strategia per nome
        }
    }

    double costo(String tipo, double peso) {
        Spedizione s = strategie.get(tipo);
        if (s == null) throw new IllegalArgumentException("Tipo sconosciuto: " + tipo);
        return s.costo(peso);
    }
}
```

- Ogni strategia è un `@Component`: Spring la trova e la registra da sola.
- `CalcoloSpedizione` non contiene nessun `if` sul tipo di spedizione.
- Per aggiungere una spedizione basta una nuova classe `@Component`.

### Avanzato

Cosa mostra: una catena di strategie con fallback. Spring le ordina con `@Order`, e la prima che si applica vince. (Import omessi.)

```java
public record Biglietto(int eta, boolean studente) { }

public interface Tariffa {
    boolean applicabile(Biglietto b);            // la strategia dichiara se si applica
    double prezzo(Biglietto b);
}

@Component
@Order(1)                                        // provata per prima
class TariffaStudente implements Tariffa {
    public boolean applicabile(Biglietto b) { return b.studente(); }
    public double prezzo(Biglietto b) { return 6.0; }
}

@Component
@Order(2)
class TariffaRidotta implements Tariffa {
    public boolean applicabile(Biglietto b) { return b.eta() < 14 || b.eta() >= 65; }
    public double prezzo(Biglietto b) { return 8.0; }
}

@Component
@Order(3)                                        // fallback: si applica sempre
class TariffaIntera implements Tariffa {
    public boolean applicabile(Biglietto b) { return true; }
    public double prezzo(Biglietto b) { return 12.0; }
}

@Service
class Botteghino {
    private final List<Tariffa> tariffe;         // Spring le inietta già in ordine di @Order

    Botteghino(List<Tariffa> tariffe) { this.tariffe = tariffe; }

    double prezzo(Biglietto b) {
        return tariffe.stream()
                .filter(t -> t.applicabile(b))   // prima strategia valida
                .findFirst()
                .orElseThrow()                   // il fallback garantisce che esista
                .prezzo(b);
    }
}
```

- `@Order` decide la priorità: numeri bassi vengono provati prima.
- Ogni strategia decide da sola se si applica (`applicabile`).
- La strategia di fallback senza condizioni evita il caso "nessuna tariffa trovata".

### Extra - Context

Cosa mostra: uitlizzo di una classe context come intermediario tra il client (main o altra classe/metodo) e le strategie da applicare.

```java
import java.util.Arrays;

// Strategy Interface (Abstract Class)
interface SortingStrategy {
    void sort(String[] arr, int size); // pure virtual function
}

// Context Class
class SortingContext {
    private SortingStrategy sortingStrategy;

    // Constructor
    public SortingContext(SortingStrategy strategy) {
        this.sortingStrategy = strategy;
    }

    // Setter to change strategy
    public void setSortingStrategy(SortingStrategy strategy) {
        this.sortingStrategy = strategy;
    }

    // Perform sort
    public void performSort(String[] arr, int size) {
        if (sortingStrategy!= null)
            sortingStrategy.sort(arr, size);
    }
}

// Example concrete strategy
class AlphabeticalSort implements SortingStrategy {
    @Override
    public void sort(String[] arr, int size) {
        Arrays.sort(arr);
    }
}

public class Client {
    public static void main(String[] args) {
        // Create SortingContext with BubbleSortStrategy
        SortingContext sortingContext = new SortingContext(new AlphabeticalSort());
        String[] array1 = {"cpp","c","java","python3","csharp","html","css","javascript","php","cpp14","cobol","dart","go","julia","kotlin","lisp","matlab","node","objc","perl","r","rust","ruby","scala","swift","solidity","xml"};
        sortingContext.performSort(array1); // Output: Sorting using Bubble Sort
```

- l'interfaccia e le classi concrete non cambiano da prima. Viene solo aggiunta una classe **"_Context"**.
- **SortingContext**: la classe che si occupa di gestire la strategia attualmente in utilizzo e di applicare le su funzioni.
- **Client**: non richiama direttamente la strategia, ma si occupa comunque di crearla e di mandarla al **Contex**. Alla fine quello che utilizza non è direttamente la *strategy* ma il *context*.

## Esercizi

### Facile

**Obiettivo:** scrivere un'interfaccia con due implementazioni e scegliere quella giusta dall'esterno.

**Consegna:** un parcheggio calcola il costo in due modi: tariffa oraria a 2 euro l'ora oppure tariffa giornaliera fissa a 10 euro. Scrivi l'interfaccia e le due strategie, poi stampa il costo di 5 ore con ciascuna.

**Soluzione:**

```java
interface TariffaParcheggio {
    double costo(int ore);
}

class TariffaOraria implements TariffaParcheggio {
    public double costo(int ore) { return ore * 2.0; }
}

class TariffaGiornaliera implements TariffaParcheggio {
    public double costo(int ore) { return 10.0; }
}

public class Main {
    public static void main(String[] args) {
        TariffaParcheggio oraria = new TariffaOraria();
        TariffaParcheggio giornaliera = new TariffaGiornaliera();

        System.out.println(oraria.costo(5));       // 10.0
        System.out.println(giornaliera.costo(5));  // 10.0
    }
}
```

Output:

```text
10.0
10.0
```

- Entrambe le strategie implementano `TariffaParcheggio`: il `main` le usa senza sapere come viene calcolato il costo.
- A 5 ore le due tariffe costano uguali (10 euro). Oltre 5 ore conviene la giornaliera, sotto conviene l'oraria.

### Medio

**Obiettivo:** usare una lista di strategie in Java puro, applicandole in sequenza.

**Consegna:** scrivi un'interfaccia `Formattazione` con il metodo `String applica(String testo)`. Crea tre strategie: tutto maiuscolo, ogni parola con la prima lettera maiuscola, testo al contrario. Applica tutte e tre alla stessa frase, una dopo l'altra, e stampa ogni risultato su una riga.

**Soluzione:**

```java
interface Formattazione {
    String applica(String testo);
}

class Maiuscolo implements Formattazione {
    public String applica(String testo) {
        return testo.toUpperCase();
    }
}

class PrimaMaiuscola implements Formattazione {
    public String applica(String testo) {
        return Arrays.stream(testo.split(" "))
                .map(p -> p.substring(0, 1).toUpperCase() + p.substring(1).toLowerCase())
                .collect(Collectors.joining(" "));
    }
}

class AlRovescio implements Formattazione {
    public String applica(String testo) {
        return new StringBuilder(testo).reverse().toString();
    }
}

public class Main {
    public static void main(String[] args) {
        List<Formattazione> strategie = List.of(new Maiuscolo(), new PrimaMaiuscola(), new AlRovescio());
        String frase = "la strategia cambia senza toccare il codice";

        for (Formattazione f : strategie) {
            System.out.println(f.applica(frase));   // stessa frase, una strategia alla volta
        }
    }
}
```

Output:

```text
LA STRATEGIA CAMBIA SENZA TOCCARE IL CODICE
La Strategia Cambia Senza Toccare Il Codice
ecidoc li eraccot aznes aibmac aigetarts al
```

- La lista `strategie` contiene le tre implementazioni: il ciclo non sa quale formattazione sta applicando.
- Ogni strategia riceve la frase originale, quindi i risultati sono indipendenti tra loro.
- Per aggiungere una formattazione basta una nuova classe e aggiungerla alla lista.

### Difficile

**Obiettivo:** usare Spring per scegliere una strategia tra più bean, con un fallback.

**Consegna:** un call center assegna una chiamata a un operatore con tre strategie: *meno occupato*, *a rotazione* e *per competenza*. Ogni strategia è un bean `@Component` con `@Order`. Il servizio `Assegnazione` riceve la lista e usa la prima strategia applicabile. Se nessun operatore è libero, la strategia di fallback deve restituire un messaggio di coda d'attesa e non lasciare il servizio senza risposta.

**Soluzione:**

Spring inietta le strategie come `List<StrategiaAssegnazione>` già ordinata per `@Order`. Per rendere distinguibili le tre strategie generiche, la chiamata ha un flag `prioritaria`: le chiamate prioritarie vanno al meno occupato, le altre a rotazione. Un operatore è *disponibile* se ha meno di 2 chiamate attive. (Import omessi.)

```java
public record Chiamata(String cliente, String competenza, boolean prioritaria) { }

public record Operatore(String nome, String competenza, int chiamateAttive) {
    static final int MAX_CHIAMATE = 2;                       // capacità massima di un operatore
    boolean disponibile() { return chiamateAttive < MAX_CHIAMATE; }
}

public interface StrategiaAssegnazione {
    boolean applicabile(Chiamata c, List<Operatore> operatori);
    String assegna(Chiamata c, List<Operatore> operatori);

    default List<Operatore> disponibili(List<Operatore> operatori) {
        return operatori.stream().filter(Operatore::disponibile).toList();
    }

    default String assegnata(Chiamata c, Operatore op) {
        return "Chiamata di " + c.cliente() + " assegnata a " + op.nome();
    }
}

@Component
@Order(1)                                                    // la più specifica: prima di tutte
class PerCompetenza implements StrategiaAssegnazione {
    public boolean applicabile(Chiamata c, List<Operatore> operatori) {
        return c.competenza() != null && disponibili(operatori).stream()
                .anyMatch(o -> o.competenza().equals(c.competenza()));
    }
    public String assegna(Chiamata c, List<Operatore> operatori) {
        Operatore op = disponibili(operatori).stream()
                .filter(o -> o.competenza().equals(c.competenza()))
                .findFirst().orElseThrow();
        return assegnata(c, op);
    }
}

@Component
@Order(2)
class MenoOccupato implements StrategiaAssegnazione {
    public boolean applicabile(Chiamata c, List<Operatore> operatori) {
        return c.prioritaria() && !disponibili(operatori).isEmpty();
    }
    public String assegna(Chiamata c, List<Operatore> operatori) {
        Operatore op = disponibili(operatori).stream()
                .min((a, b) -> Integer.compare(a.chiamateAttive(), b.chiamateAttive()))
                .orElseThrow();
        return assegnata(c, op);
    }
}

@Component
@Order(3)
class ARotazione implements StrategiaAssegnazione {
    private final AtomicInteger prossimo = new AtomicInteger(0);

    public boolean applicabile(Chiamata c, List<Operatore> operatori) {
        return !disponibili(operatori).isEmpty();
    }
    public String assegna(Chiamata c, List<Operatore> operatori) {
        List<Operatore> liberi = disponibili(operatori);
        Operatore op = liberi.get(Math.floorMod(prossimo.getAndIncrement(), liberi.size()));
        return assegnata(c, op);
    }
}

@Component
@Order(4)                                                    // fallback: nessun operatore disponibile
class CodaAttesa implements StrategiaAssegnazione {
    public boolean applicabile(Chiamata c, List<Operatore> operatori) { return true; }
    public String assegna(Chiamata c, List<Operatore> operatori) {
        return "Chiamata di " + c.cliente() + " in coda d'attesa";
    }
}

@Service
class Assegnazione {
    private final List<StrategiaAssegnazione> strategie;     // Spring le inietta già in ordine di @Order

    Assegnazione(List<StrategiaAssegnazione> strategie) { this.strategie = strategie; }

    String assegna(Chiamata c, List<Operatore> operatori) {
        return strategie.stream()
                .filter(s -> s.applicabile(c, operatori))     // prima strategia applicabile
                .findFirst()
                .orElseThrow()                                // il fallback garantisce che esista
                .assegna(c, operatori);
    }
}
```

Output (con questa demo nel `main`):

```text
Chiamata di Rossi assegnata a Anna
Chiamata di Bianchi assegnata a Anna
Chiamata di Verdi assegnata a Anna
Chiamata di Gialli assegnata a Luca
Chiamata di Neri in coda d'attesa
```

- `Assegnazione` non contiene nessun `if`: sceglie la prima strategia la cui `applicabile` è vera.
- Con `@Order` la più specifica (competenza) viene provata per prima, il fallback `CodaAttesa` per ultimo.
- `ARotazione` usa un contatore: `Verdi` e `Gialli` vanno ad Anna e Luca a turno, saltando Marco che è occupato.
- Se tutti sono occupati, nessuna strategia tranne il fallback è applicabile, e il cliente riceve il messaggio di coda.

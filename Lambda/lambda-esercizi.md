# Lambda: 3 esercizi per gli studenti

## Esercizio 1 (Facile): lambda con zero, uno e più parametri

**Obiettivo:** scrivere lambda con un parametro e con due parametri, usando solo liste e array. Niente stream e niente interfacce funzionali da dichiarare o nominare.

**Compito:** in una classe `Esercizio1` con un `main` (con gli import necessari), scrivi tre lambda e usale:

1. **Un parametro:** crea `List<Integer> numeri = List.of(1, 2, 3)` e usa `forEach` con una lambda che stampa ogni numero moltiplicato per 2.
2. **Due parametri:** crea l'array `String[] parole = {"mondo", "a", "ciao"}` e ordinalo per lunghezza con `Arrays.sort(parole, (x, y) -> ...)`. Poi stampa `Arrays.toString(parole)`.

### Soluzione esercizio 1

```java
import java.util.Arrays;
import java.util.List;

public class Esercizio1 {
    public static void main(String[] args) {
        // 1. Un parametro: la lambda riceve un solo Integer, n
        List<Integer> numeri = List.of(1, 2, 3);
        numeri.forEach(n -> System.out.println(n * 2));

        // 2. Due parametri: la lambda riceve due String, x e y,
        //    e restituisce un int negativo, zero o positivo
        String[] parole = {"mondo", "a", "ciao"};
        Arrays.sort(parole, (x, y) -> x.length() - y.length());
        System.out.println(Arrays.toString(parole));
    }
}
```

**Output atteso:**

```text
2
4
6
[a, ciao, mondo]
```

**Spiegazione:**

- Con **un parametro** la sintassi è `n -> ...`: le parentesi tonde si possono omettere quando c'è un solo parametro.
- Con **due parametri** servono le parentesi: `(x, y) -> ...`. `Arrays.sort` le usa per confrontare due elementi: se `x.length() - y.length()` è negativo, `x` va prima di `y`.
Il titolo parla anche di lambda **senza** parametri. Un esempio, se vuoi provarlo:

```java
Runnable saluto = () -> System.out.println("ciao");
saluto.run();
```

---

## Esercizio 2 (Medio): `Predicate`, `Comparator`, `Consumer`

**Obiettivo:** usare le tre interfacce funzionali con i metodi della lista (`forEach`, `removeIf`, `sort`), senza stream.

**Dati:**

```java
List<String> frutta = new ArrayList<>(
        List.of("kiwi", "banana", "mela", "ananas", "pera", "fico", "arancia"));
```

**Compito:**

1. Scrivi un `Consumer<String> stampa` che stampa la parola, e usalo con `forEach` per mostrare la lista iniziale.
2. Scrivi un `Predicate<String> corta` vera per le parole con **4 o meno** caratteri, e usalo con `removeIf`.
3. Ordina la lista con un `Comparator<String>`: prima le parole **più lunghe**, e a parità di lunghezza in **ordine alfabetico**.
4. Stampa di nuovo la lista con lo stesso `Consumer`, dopo una riga `---`.

**Suggerimento:** il `Comparator` si può scrivere come lambda a due parametri `(a, b) -> ...` che restituisce un numero negativo, zero o positivo, oppure con `Comparator.comparingInt(...)`.

### Soluzione esercizio 2

```java
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class Esercizio2 {
    public static void main(String[] args) {
        // List.of produce una lista immutabile: removeIf e sort non funzionerebbero.
        // Per questo la copiamo in un ArrayList modificabile.
        List<String> frutta = new ArrayList<>(
                List.of("kiwi", "banana", "mela", "ananas", "pera", "fico", "arancia"));

        // 1. Consumer: riceve una String e non restituisce nulla
        Consumer<String> stampa = parola -> System.out.println(parola);
        frutta.forEach(stampa);

        // 2. Predicate: riceve una String e restituisce true/false
        Predicate<String> corta = parola -> parola.length() <= 4;
        frutta.removeIf(corta);

        // 3. Comparator: prima le più lunghe, a parità di lunghezza in ordine alfabetico
        Comparator<String> piuLungaPoiAlfabetica = (a, b) -> {
            if (a.length() != b.length()) {
                return b.length() - a.length(); // b più corta di a -> a viene prima
            }
            return a.compareTo(b);              // stessa lunghezza -> ordine alfabetico
        };
        frutta.sort(piuLungaPoiAlfabetica);

        // 4. Stessa stampa di prima, dopo la riga di separazione
        System.out.println("---");
        frutta.forEach(stampa);
    }
}
```

**Output atteso:**

```text
kiwi
banana
mela
ananas
pera
fico
arancia
---
arancia
ananas
banana
```

**Spiegazione:**

- `Consumer<String>` ha un metodo `accept` che non restituisce niente: è adatto a `forEach` perché si limita a "fare qualcosa" con ogni elemento. Lo stesso oggetto `stampa` si riusa due volte.
- `Predicate<String>` restituisce `true` per le parole da **rimuovere**: `removeIf` elimina tutti gli elementi per cui il predicato è vero (`kiwi`, `mela`, `pera`, `fico`).
- Il `Comparator` restituisce `b.length() - a.length()` quando le lunghezze sono diverse: così le parole più lunghe finiscono prima. Quando sono uguali, `a.compareTo(b)` ordina alfabeticamente.
**Alternativa** con i metodi di `Comparator`, equivalente:

```java
Comparator<String> alternativa = Comparator.comparingInt(String::length)
        .reversed()
        .thenComparing(Comparator.naturalOrder());
```

---

## Esercizio 3 (Difficile): stream

**Compito:** con gli stream, ricava:

1. i **nomi dei prodotti** senza doppioni, in ordine alfabetico;
2. il **totale incassato** (somma dei `totale()` di tutti gli ordini);
3. i **clienti** con almeno un ordine da **più di 100 euro**, senza doppioni e in ordine alfabetico;
4. l'ordine con il totale più alto, come testo `cliente - prodotto`.

**Dati:**

```java
public record Ordine(String cliente, String prodotto, int quantita, double prezzoUnitario) {
    double totale() { return quantita * prezzoUnitario; }
}

List<Ordine> ordini = List.of(
        new Ordine("Anna", "Mouse", 2, 25.0),
        new Ordine("Luca", "Monitor", 1, 199.0),
        new Ordine("Anna", "Tastiera", 1, 45.0),
        new Ordine("Marta", "Mouse", 3, 25.0),
        new Ordine("Luca", "Webcam", 2, 60.0),
        new Ordine("Paolo", "Monitor", 2, 199.0));
```

### Soluzione esercizio 3

```java
import java.util.Comparator;
import java.util.List;

public class Esercizio3 {

    public record Ordine(String cliente, String prodotto, int quantita, double prezzoUnitario) {
        double totale() { return quantita * prezzoUnitario; }
    }

    public static void main(String[] args) {
        List<Ordine> ordini = List.of(
                new Ordine("Anna", "Mouse", 2, 25.0),
                new Ordine("Luca", "Monitor", 1, 199.0),
                new Ordine("Anna", "Tastiera", 1, 45.0),
                new Ordine("Marta", "Mouse", 3, 25.0),
                new Ordine("Luca", "Webcam", 2, 60.0),
                new Ordine("Paolo", "Monitor", 2, 199.0));

        // 1. Nomi dei prodotti, senza doppioni, in ordine alfabetico
        List<String> prodotti = ordini.stream()
                .map(Ordine::prodotto)   // Stream<String> con i nomi dei prodotti
                .distinct()              // elimina i doppioni (Mouse compare 2 volte)
                .sorted()                // ordine alfabetico
                .toList();
        System.out.println(prodotti);
        // [Monitor, Mouse, Tastiera, Webcam]

        // 2. Totale incassato: somma dei totali di tutti gli ordini
        double incasso = ordini.stream()
                .mapToDouble(Ordine::totale)   // Stream di double, con i totali
                .sum();
        System.out.println(incasso);
        // 887.0

        // 3. Clienti con almeno un ordine da più di 100 euro
        List<String> clienti = ordini.stream()
                .filter(o -> o.totale() > 100) // solo gli ordini sopra 100 euro
                .map(Ordine::cliente)
                .distinct()                    // un cliente può comparire più volte
                .sorted()
                .toList();
        System.out.println(clienti);
        // [Luca, Paolo]

        // 4. Ordine con il totale più alto, come testo "cliente - prodotto"
        String top = ordini.stream()
                .max(Comparator.comparingDouble(Ordine::totale)) // Optional<Ordine>
                .map(o -> o.cliente() + " - " + o.prodotto())    // Optional<String>
                .orElse("nessun ordine");                        // se la lista è vuota
        System.out.println(top);
        // Paolo - Monitor
    }
}
```

**Spiegazione:**

- **`distinct()`** rimuove i duplicati (in `distinct` il confronto usa `equals`). Va messo prima di `sorted()` o dopo, a seconda di quello che serve; qui funziona in entrambi i casi.
- **`mapToDouble(...).sum()`** è più comodo di `map(...).reduce(...)`, perché restituisce direttamente un `double`. Il totale è `50 + 199 + 45 + 75 + 120 + 398 = 887`.
- Per il punto 3, i totali sopra 100 sono: Luca (Monitor, 199), Luca (Webcam, 120) e Paolo (Monitor, 398). Anna e Marta non superano 100 con nessun ordine.
- Per il punto 4, **`max`** richiede un `Comparator`; `Comparator.comparingDouble(Ordine::totale)` confronta i totali. Il risultato è un `Optional`, che può essere vuoto se la lista è vuota: per questo si usa `orElse`.
- Il metodo `totale()` del record non è `public`, ma è accessibile da `Esercizio3` perché il record è dichiarato nella stessa classe.

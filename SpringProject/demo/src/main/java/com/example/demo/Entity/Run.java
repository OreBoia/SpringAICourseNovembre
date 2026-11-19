package com.example.demo.Entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

import com.example.demo.Enum.Location;

@Entity
@Table(name = "runs")
public class Run {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotBlank(message = "Il titolo non può essere vuoto")
    @Size(min = 3, max = 100, message = "Il titolo deve avere tra 3 e 100 caratteri")
    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @NotNull(message = "La data di inizio è obbligatoria")
    @Column(name = "started_on", nullable = false)
    private LocalDateTime startedOn;

    @NotNull(message = "La data di fine è obbligatoria")
    @Column(name = "completed_on", nullable = false)
    private LocalDateTime completedOn;

    @Positive(message = "I chilometri devono essere un valore positivo")
    @Max(value = 200, message = "Non si possono registrare più di 200 miglia")
    @Column(name = "miles", nullable = false)
    private double miles;

    @NotNull(message = "La location è obbligatoria")
    @Enumerated(EnumType.STRING)  // salva il nome dell'enum come stringa ("INDOOR"/"OUTDOOR")
    @Column(name = "location", nullable = false)
    private Location location;

    // ── Costruttore no-arg richiesto da JPA ──────────────────────────────────
    protected Run() {}

    // ── Costruttore completo per uso applicativo ─────────────────────────────
    public Run(String title, LocalDateTime startedOn, LocalDateTime completedOn,
               double miles, Location location) {
        validateBusinessRules(startedOn, completedOn, miles);
        this.title = title;
        this.startedOn = startedOn;
        this.completedOn = completedOn;
        this.miles = miles;
        this.location = location;
    }

    // ── Aggiornamento con le stesse regole del costruttore ───────────────────
    public void update(String title, LocalDateTime startedOn, LocalDateTime completedOn,
                       double miles, Location location) {
        validateBusinessRules(startedOn, completedOn, miles);
        this.title = title;
        this.startedOn = startedOn;
        this.completedOn = completedOn;
        this.miles = miles;
        this.location = location;
    }

    private static void validateBusinessRules(LocalDateTime startedOn, LocalDateTime completedOn, double miles) {
        if (miles < 0) throw new IllegalArgumentException("Le miglia non possono essere negative");
        if (startedOn != null && completedOn != null && completedOn.isBefore(startedOn))
            throw new IllegalArgumentException("La data di fine deve essere dopo quella di inizio");
    }

    // ── Getter ───────────────────────────────────────────────────────────────
    public Integer getId() { return id; }
    public String getTitle() { return title; }
    public LocalDateTime getStartedOn() { return startedOn; }
    public LocalDateTime getCompletedOn() { return completedOn; }
    public double getMiles() { return miles; }
    public Location getLocation() { return location; }

    // ── Setter ───────────────────────────────────────────────────────────────
    public void setTitle(String title) { this.title = title; }
    public void setStartedOn(LocalDateTime startedOn) { this.startedOn = startedOn; }
    public void setCompletedOn(LocalDateTime completedOn) { this.completedOn = completedOn; }
    public void setMiles(double miles) { this.miles = miles; }
    public void setLocation(Location location) { this.location = location; }

    // ── toString ─────────────────────────────────────────────────────────────
    @Override
    public String toString() {
        return "Run{id=" + id + ", title='" + title + "', miles=" + miles + ", location=" + location + "}";
    }
}

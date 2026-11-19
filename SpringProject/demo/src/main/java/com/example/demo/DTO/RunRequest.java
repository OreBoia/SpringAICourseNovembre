package com.example.demo.DTO;

import java.time.LocalDateTime;

import com.example.demo.Enum.Location;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

// DTO di input: nessun campo id (lo genera il database)
public record RunRequest(

        @NotBlank(message = "Il titolo non può essere vuoto")
        @Size(min = 3, max = 100, message = "Il titolo deve avere tra 3 e 100 caratteri")
        String title,

        @NotNull(message = "La data di inizio è obbligatoria")
        LocalDateTime startedOn,

        @NotNull(message = "La data di fine è obbligatoria")
        LocalDateTime completedOn,

        @Positive(message = "I chilometri devono essere un valore positivo")
        @Max(value = 200, message = "Non si possono registrare più di 200 miglia")
        double miles,

        @NotNull(message = "La location è obbligatoria")
        Location location
) {}

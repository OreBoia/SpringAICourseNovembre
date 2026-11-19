package com.example.demo.DTO;

import java.time.LocalDateTime;

// Un Record è perfetto per oggetti immutabili come la risposta di errore
public record ErrorResponse(
        String message,
        int status,
        LocalDateTime timestamp
) {}

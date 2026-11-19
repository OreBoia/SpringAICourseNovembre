package com.example.demo.Exception;

// Estende RuntimeException: eccezione non verificata (unchecked)
public class RunNotFoundException extends RuntimeException {

    public RunNotFoundException(Integer id) {
        super("Run con id " + id + " non trovato");
    }
}

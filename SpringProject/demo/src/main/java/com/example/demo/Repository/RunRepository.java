package com.example.demo.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.Entity.Run;
import com.example.demo.Enum.Location;

public interface RunRepository extends JpaRepository<Run, Integer> {

    // Trova tutte le corse con un titolo specifico
    List<Run> findByTitle(String title);

    // Trova tutte le corse con miglia maggiori di una soglia
    List<Run> findByMilesGreaterThan(double miles);

    // Trova corse per location
    List<Run> findByLocation(Location location);

    // Combina condizioni con AND
    List<Run> findByLocationAndMilesGreaterThan(Location location, double miles);

    // Trova la prima corsa per titolo (Optional per sicurezza)
    Optional<Run> findFirstByTitle(String title);

    // Verifica se esiste una corsa con un certo titolo
    boolean existsByTitle(String title);
}
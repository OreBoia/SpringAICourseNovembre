package com.example.demo.Service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.demo.DTO.RunRequest;
import com.example.demo.DTO.RunResponse;
import com.example.demo.Entity.Run;
import com.example.demo.Exception.RunNotFoundException;
import com.example.demo.Repository.RunRepository;

@Service
public class RunService {

    private final RunRepository runRepository;

    // Iniezione via costruttore (raccomandata)
    public RunService(RunRepository runRepository) {
        this.runRepository = runRepository;
    }

    public List<RunResponse> findAll() {
        return runRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public RunResponse findById(Integer id) {
        return toResponse(findEntityById(id));
    }

    public RunResponse save(RunRequest request) {
        Run saved = runRepository.save(toEntity(request));
        return toResponse(saved);
    }

    public RunResponse update(Integer id, RunRequest request) {
        Run existing = findEntityById(id);
        existing.update(
                request.title(),
                request.startedOn(),
                request.completedOn(),
                request.miles(),
                request.location()
        );
        return toResponse(runRepository.save(existing));
    }

    public void deleteById(Integer id) {
        if (!runRepository.existsById(id)) {
            throw new RunNotFoundException(id);
        }
        runRepository.deleteById(id);
    }

    private Run findEntityById(Integer id) {
        return runRepository.findById(id)
                .orElseThrow(() -> new RunNotFoundException(id));
    }

    // Mapping: RunRequest → Run (entità JPA)
    private Run toEntity(RunRequest request) {
        return new Run(
                request.title(),
                request.startedOn(),
                request.completedOn(),
                request.miles(),
                request.location()
        );
    }

    // Mapping: Run (entità JPA) → RunResponse
    private RunResponse toResponse(Run run) {
        return new RunResponse(
                run.getId(),
                run.getTitle(),
                run.getStartedOn(),
                run.getCompletedOn(),
                run.getMiles(),
                run.getLocation().name()  // converte enum in stringa
        );
    }
}

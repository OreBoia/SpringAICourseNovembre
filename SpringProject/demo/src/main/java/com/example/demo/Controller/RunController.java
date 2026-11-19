package com.example.demo.Controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import com.example.demo.DTO.RunRequest;
import com.example.demo.DTO.RunResponse;
import com.example.demo.Service.RunService;

@RestController
@RequestMapping("/api/runs")   // prefisso comune per tutti gli endpoint di questa classe
public class RunController {

    private final RunService runService;

    public RunController(RunService runService) {
        this.runService = runService;
    }

    @GetMapping
    public List<RunResponse> findAll() {
        return runService.findAll();
    }

    @GetMapping("/{id}")
    public RunResponse findById(@PathVariable Integer id) {
        return runService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RunResponse create(@Valid @RequestBody RunRequest request) {
        return runService.save(request);
    }

    @PutMapping("/{id}")
    public RunResponse update(@PathVariable Integer id, @Valid @RequestBody RunRequest request) {
        return runService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Integer id) {
        runService.deleteById(id);
    }
}

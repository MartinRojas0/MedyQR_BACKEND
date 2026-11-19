package com.mediqr.backend.controller;

import com.mediqr.backend.dto.PersonalSaludCreateRequest;
import com.mediqr.backend.dto.PersonalSaludUpdateRequest;
import com.mediqr.backend.model.PersonalSalud;
import com.mediqr.backend.service.PersonalSaludService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/personal-salud")
public class PersonalSaludController {

    private final PersonalSaludService personalSaludService;

    public PersonalSaludController(PersonalSaludService personalSaludService) {
        this.personalSaludService = personalSaludService;
    }

    @GetMapping
    public List<PersonalSalud> getAll() {
        return personalSaludService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<PersonalSalud> getById(@PathVariable Long id) {
        return ResponseEntity.ok(personalSaludService.getById(id));
    }

    @PostMapping
    public ResponseEntity<PersonalSalud> create(@Valid @RequestBody PersonalSaludCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(personalSaludService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PersonalSalud> update(@PathVariable Long id,
                                                @Valid @RequestBody PersonalSaludUpdateRequest request) {
        return ResponseEntity.ok(personalSaludService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        personalSaludService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
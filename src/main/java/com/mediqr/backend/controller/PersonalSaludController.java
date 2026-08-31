package com.mediqr.backend.controller;

import com.mediqr.backend.model.PersonalSalud;
import com.mediqr.backend.service.PersonalSaludService;
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
        return personalSaludService.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<PersonalSalud> create(@RequestBody PersonalSalud personalSalud) {
        PersonalSalud savedPersonalSalud = personalSaludService.save(personalSalud);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedPersonalSalud);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PersonalSalud> update(@PathVariable Long id, @RequestBody PersonalSalud personalSalud) {
        return personalSaludService.update(id, personalSalud)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (personalSaludService.findById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        personalSaludService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}

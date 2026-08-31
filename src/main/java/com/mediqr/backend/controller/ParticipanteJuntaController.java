package com.mediqr.backend.controller;

import com.mediqr.backend.model.ParticipanteJunta;
import com.mediqr.backend.service.ParticipanteJuntaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/participantes-junta")
public class ParticipanteJuntaController {

    private final ParticipanteJuntaService participanteJuntaService;

    public ParticipanteJuntaController(ParticipanteJuntaService participanteJuntaService) {
        this.participanteJuntaService = participanteJuntaService;
    }

    @GetMapping
    public List<ParticipanteJunta> getAll() {
        return participanteJuntaService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ParticipanteJunta> getById(@PathVariable Long id) {
        return participanteJuntaService.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<ParticipanteJunta> create(@RequestBody ParticipanteJunta participanteJunta) {
        ParticipanteJunta savedParticipanteJunta = participanteJuntaService.save(participanteJunta);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedParticipanteJunta);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ParticipanteJunta> update(@PathVariable Long id, @RequestBody ParticipanteJunta participanteJunta) {
        return participanteJuntaService.update(id, participanteJunta)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (participanteJuntaService.findById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        participanteJuntaService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}

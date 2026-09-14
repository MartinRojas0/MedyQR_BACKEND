package com.mediqr.backend.repository;

import com.mediqr.backend.model.PersonalSalud;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PersonalSaludRepository extends JpaRepository<PersonalSalud, Long> {
    Optional<PersonalSalud> findByUsuarioId(Long usuarioId);
}

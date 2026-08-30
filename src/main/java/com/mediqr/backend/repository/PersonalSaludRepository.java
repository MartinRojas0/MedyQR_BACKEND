package com.mediqr.backend.repository;

import com.mediqr.backend.model.PersonalSalud;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PersonalSaludRepository extends JpaRepository<PersonalSalud, Long> {
}

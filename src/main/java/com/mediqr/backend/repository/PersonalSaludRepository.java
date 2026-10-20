package com.mediqr.backend.repository;

import com.mediqr.backend.model.PersonalSalud;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PersonalSaludRepository extends JpaRepository<PersonalSalud, Long> {
boolean existsByUsuarioId(Long usuarioId);
boolean existsByDocumentoIdentidad(String documentoIdentidad);
boolean existsByDocumentoIdentidadAndIdNot(String documentoIdentidad, Long id);
boolean existsByRegistroProfesional(String registroProfesional);
boolean existsByRegistroProfesionalAndIdNot(String registroProfesional, Long id);
}

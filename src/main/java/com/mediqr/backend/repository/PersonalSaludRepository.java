package com.mediqr.backend.repository;

import com.mediqr.backend.model.PersonalSalud;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PersonalSaludRepository extends JpaRepository<PersonalSalud, Long> {
    boolean existsByUsuarioId(Long usuarioId);
    boolean existsByDocumentoIdentidad(String documentoIdentidad);
    boolean existsByDocumentoIdentidadAndIdNot(String documentoIdentidad, Long id);
    boolean existsByRegistroProfesional(String registroProfesional);
    boolean existsByRegistroProfesionalAndIdNot(String registroProfesional, Long id);
    Optional<PersonalSalud> findByUsuarioId(Long usuarioId);
    List<PersonalSalud> findAllByActivoTrueOrderByApellidosAscNombresAsc();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from PersonalSalud p where p.id = :id")
    Optional<PersonalSalud> findByIdForUpdate(@Param("id") Long id);
}

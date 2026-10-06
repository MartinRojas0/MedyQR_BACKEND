package com.mediqr.backend.repository;

import com.mediqr.backend.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
boolean existsByEmailIgnoreCase(String email);
boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);
}

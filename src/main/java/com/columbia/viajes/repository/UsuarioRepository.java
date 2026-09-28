package com.columbia.viajes.repository;

import com.columbia.viajes.model.Usuario;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {
    Optional<Usuario> findByNombre(String nombre);
}

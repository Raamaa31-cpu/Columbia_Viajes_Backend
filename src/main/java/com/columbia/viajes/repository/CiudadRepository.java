package com.columbia.viajes.repository;

import com.columbia.viajes.model.Ciudad;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CiudadRepository extends JpaRepository<Ciudad, Integer> {

    boolean existsByNombre(String nombre);
    
    Optional<Ciudad> findByNombre(String nombre);
    
}

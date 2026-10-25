package com.columbia.viajes.repository;

import com.columbia.viajes.model.Ciudad;
import java.util.Optional;

public interface CiudadRepository extends RepositorioConNombre<Ciudad> {

    boolean existsByNombre(String nombre);
    
    Optional<Ciudad> findByNombre(String nombre);
    
}

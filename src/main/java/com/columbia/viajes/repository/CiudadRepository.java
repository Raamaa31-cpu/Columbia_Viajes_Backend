package com.columbia.viajes.repository;

import com.columbia.viajes.model.Ciudad;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CiudadRepository extends JpaRepository<Ciudad, Integer> {
    
}

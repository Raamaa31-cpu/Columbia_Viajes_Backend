package com.columbia.viajes.repository;

import com.columbia.viajes.model.Paquete;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaqueteRepository extends JpaRepository<Paquete, Integer> {
    
}

package com.columbia.viajes.repository;

import com.columbia.viajes.model.Turista;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TuristaRepository extends JpaRepository<Turista, Integer> {
    
}

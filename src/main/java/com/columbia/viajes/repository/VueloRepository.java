package com.columbia.viajes.repository;

import com.columbia.viajes.model.Vuelo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VueloRepository extends JpaRepository<Vuelo, Integer> {
    
}

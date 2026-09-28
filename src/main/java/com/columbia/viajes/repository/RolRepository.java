package com.columbia.viajes.repository;

import com.columbia.viajes.model.Rol;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RolRepository extends JpaRepository<Rol, Integer> {
    
}

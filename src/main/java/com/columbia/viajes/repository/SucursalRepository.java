package com.columbia.viajes.repository;

import com.columbia.viajes.model.Sucursal;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SucursalRepository extends JpaRepository<Sucursal, Integer> {
    
}

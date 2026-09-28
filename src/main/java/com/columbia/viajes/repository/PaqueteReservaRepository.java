package com.columbia.viajes.repository;

import com.columbia.viajes.model.PaqueteReserva;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PaqueteReservaRepository extends JpaRepository<PaqueteReserva, Integer> {    
    
    @Query("""
       SELECT v.id, v.nombre, SUM(r.precioReserva)
       FROM PaqueteReserva r
       JOIN r.vendedor v
       GROUP BY v.id, v.nombre
       ORDER BY SUM(r.precioReserva) DESC
       """)
    List<Object[]> rankingVendedores();
}

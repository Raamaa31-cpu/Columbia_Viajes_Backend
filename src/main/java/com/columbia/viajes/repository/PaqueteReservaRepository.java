package com.columbia.viajes.repository;

import com.columbia.viajes.model.PaqueteReserva;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PaqueteReservaRepository extends JpaRepository<PaqueteReserva, Integer> {    
    
    @Query("""
       SELECT v.id, v.nombre, SUM(r.precioReserva), v.sucursal.id
       FROM PaqueteReserva r
       JOIN r.vendedor v
       GROUP BY v.id, v.nombre, v.sucursal.id
       ORDER BY SUM(r.precioReserva) DESC
       """)
    List<Object[]> rankingVendedores();
    
    List<PaqueteReserva> findByPaquete_Sucursal_Id(Integer idSucursal);
    
    List<PaqueteReserva> findByTurista_Id(Integer idTurista);

    Optional<PaqueteReserva> findByIdAndPaquete_Sucursal_Id(Integer id, Integer idSucursal);

    Optional<PaqueteReserva> findByIdAndTurista_Id(Integer id, Integer idTurista);
}

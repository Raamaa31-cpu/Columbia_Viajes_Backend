package com.columbia.viajes.repository;

import com.columbia.viajes.model.Paquete;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PaqueteRepository extends RepositorioConNombre<Paquete> {
    
    @Query("""
           SELECT p FROM Paquete p
           WHERE(:nombre IS NULL
                OR LOWER(p.nombre) LIKE LOWER(CONCAT('%', :nombre, '%')))
           AND (:idHotel IS NULL OR p.hotel.id = :idHotel)
           AND (:idCiudad IS NULL or p.hotel.ciudad.id = :idCiudad)
           """)
    List<Paquete> buscar(
        @Param("nombre") String nombre,
        @Param("idCiudad") Integer idCiudad,
        @Param("idHotel") Integer idHotel
    );
}

package com.columbia.viajes.repository;

import com.columbia.viajes.model.Hotel;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface HotelRepository extends RepositorioConNombre<Hotel> {
    @Query("""
        SELECT h FROM Hotel h
        WHERE (:nombre IS NULL
                OR LOWER(h.nombre) LIKE LOWER(CONCAT('%', :nombre, '%')))
        AND (:idCiudad IS NULL OR h.ciudad.id = :idCiudad)
        """)
    List<Hotel> buscar(
            @Param("nombre") String nombre,
            @Param("idCiudad") Integer idCiudad
    );
}

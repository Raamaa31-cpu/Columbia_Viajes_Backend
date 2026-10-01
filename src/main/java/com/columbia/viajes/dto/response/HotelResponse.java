package com.columbia.viajes.dto.response;

public record HotelResponse(
        Integer id,
        String nombre,
        String direccion,
        String ciudadNombre,
        String telefono,
        Integer plazasTotales
) {}

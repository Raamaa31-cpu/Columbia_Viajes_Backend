package com.columbia.viajes.dto.response;

public record TuristaResponse(
        Integer id,
        String nombre,
        String apellidos,
        String direccion,
        String email,
        String telefono,
        Integer usuarioId
) {}

package com.columbia.viajes.dto.response;

import java.time.LocalDateTime;

public record UsuarioResponse(
        Integer id,
        String nombre,
        String rolNombre,
        LocalDateTime fechaCreacion,
        Integer sucursalId
) {}
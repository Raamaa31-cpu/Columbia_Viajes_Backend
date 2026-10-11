package com.columbia.viajes.dto.response;

public record AuthResponse(
        String nombre,
        String rolNombre,
        String token
) {}

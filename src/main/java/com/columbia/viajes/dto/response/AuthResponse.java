package com.columbia.viajes.dto.response;

/**
 * DTO para devolver la respuesta del login.
 *
 * <p>Incluye el token JWT y algunos datos básicos del usuario para que el
 * frontend no tenga que hacer una request adicional al loguearse.</p>
 */
public record AuthResponse(
        String token,
        Integer usuarioId,
        String nombre,
        String rolNombre
) {}

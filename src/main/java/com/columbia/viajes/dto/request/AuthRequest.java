package com.columbia.viajes.dto.request;

import jakarta.validation.constraints.NotBlank;

public record AuthRequest(
        @NotBlank(message = "El nombre de usuario no puede estar vacío")
        String nombre,
        
        @NotBlank(message = "La contraseña no puede estar vacía")
        String contrasenia
        
) {}

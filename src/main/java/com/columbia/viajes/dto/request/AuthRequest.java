package com.columbia.viajes.dto.request;

import jakarta.validation.constraints.NotBlank;

public record AuthRequest(
        @NotBlank(message = "El email o nombre de usuario no puede estar vacío")
        String identificador,
        
        @NotBlank(message = "La contraseña no puede estar vacía")
        String contrasenia
        
) {}

package com.columbia.viajes.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CambioContraseniaRequest(
        @NotBlank(message = "La contraseña actual no puede estar vacía")
        String contraseniaActual,
        
        @NotBlank(message = "La contraseña nueva no puede estar vacía")
        @Size(min = 8, max = 64, message = "La contraseña nueva debe tener entre 8 y 64 caracteres")
        String contraseniaNueva
) {}

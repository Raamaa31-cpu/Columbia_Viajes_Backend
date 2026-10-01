package com.columbia.viajes.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SucursalRequest(
        @NotBlank(message = "La dirección de la sucursal no puede estar vacía")
        @Size(min = 5, max = 150, message = "La dirección debe tener entre 5 y 150 caracteres")
        String direccion,

        @NotBlank(message = "El email no puede estar vacío")
        @Size(min = 5, max = 120, message = "El email debe tener entre 5 y 120 caracteres")
        @Email(message = "El email debe ser un correo electrónico válido")
        String email,

        @NotBlank(message = "El teléfono no puede estar vacío")
        @Size(min = 7, max = 20, message = "El teléfono debe tener entre 7 y 20 caracteres")
        String telefono
) {}

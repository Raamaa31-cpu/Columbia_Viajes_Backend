package com.columbia.viajes.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegistroClienteRequest(
        @NotBlank(message = "El nombre de usuario no puede estar vacío")
        @Size(min = 2, max = 100, message = "El nombre de usuario debe tener entre 2 y 100 caracteres")
        String nombreUsuario,

        @NotBlank(message = "La contraseña no puede estar vacía")
        @Size(min = 8, max = 64, message = "La contraseña debe tener entre 8 y 64 caracteres")
        String contrasenia,

        @NotNull(message = "Los datos del turista son obligatorios")
        @Valid
        TuristaRequest turista
) {}

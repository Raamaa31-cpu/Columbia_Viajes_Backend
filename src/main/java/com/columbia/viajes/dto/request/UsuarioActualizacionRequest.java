package com.columbia.viajes.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UsuarioActualizacionRequest(
        @NotBlank(message = "El nombre de usuario no puede estar vacío")
        @Size(min = 2, max = 100, message = "El nombre de usuario debe tener entre 2 y 100 caracteres")
        String nombre,
        
        @NotNull(message = "El ID del rol es obligatorio")
        Integer idRol,
        
        Integer idSucursal
) {}

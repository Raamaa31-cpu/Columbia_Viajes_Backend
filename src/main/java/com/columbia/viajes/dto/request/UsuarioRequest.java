package com.columbia.viajes.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UsuarioRequest(

    @NotBlank(message = "El nombre de usuario no puede estar vacío")
    @Size(min = 2, max = 100, message = "El nombre de usuario debe tener entre 2 y 100 caracteres")
    String nombre,

    @NotBlank(message = "La contraseña no puede estar vacía")
    @Size(min = 8, max = 64, message = "La contraseña debe tener entre 8 y 64 caracteres")
    String contrasenia,

    @NotNull(message = "El ID del rol es obligatorio")
    Integer idRol,

    Integer idSucursal
) {}

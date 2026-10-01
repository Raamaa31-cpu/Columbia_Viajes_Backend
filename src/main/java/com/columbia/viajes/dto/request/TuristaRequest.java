package com.columbia.viajes.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record TuristaRequest(

    @NotBlank(message = "El nombre del turista no puede estar vacío")
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    String nombre,

    @NotBlank(message = "Los apellidos no pueden estar vacíos")
    @Size(min = 2, max = 100, message = "Los apellidos deben tener entre 2 y 100 caracteres")
    String apellidos,

    @NotBlank(message = "La dirección no puede estar vacía")
    @Size(min = 5, max = 150, message = "La dirección debe tener entre 5 y 150 caracteres")
    String direccion,

    @NotBlank(message = "El email no puede estar vacío")
    @Email(message = "El email debe ser un correo electrónico válido")
    @Size(min = 5, max = 120, message = "El email debe tener entre 5 y 120 caracteres")
    String email,

    @NotBlank(message = "El teléfono no puede estar vacío")
    @Size(min = 7, max = 20, message = "El teléfono debe tener entre 7 y 20 caracteres")
    String telefono,

    @NotNull(message = "El ID del usuario es obligatorio")
    Integer idUsuario
) {}

package com.columbia.viajes.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record HotelRequest(

    @NotBlank(message = "El nombre del hotel no puede estar vacío")
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    String nombre,

    @NotBlank(message = "La dirección no puede estar vacía")
    @Size(min = 5, max = 150, message = "La dirección debe tener entre 5 y 150 caracteres")
    String direccion,

    @NotNull(message = "El ID de la ciudad es obligatorio")
    Integer idCiudad,

    @NotBlank(message = "El teléfono no puede estar vacío")
    @Size(min = 7, max = 20, message = "El teléfono debe tener entre 7 y 20 caracteres")
    String telefono,

    @NotNull(message = "La cantidad de plazas totales es obligatoria")
    @Min(value = 0, message = "Las plazas totales no pueden ser un valor negativo")
    Integer plazasTotales
) {}

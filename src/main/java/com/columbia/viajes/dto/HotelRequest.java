/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Record.java to edit this template
 */
package com.columbia.viajes.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 *
 * @author Ramiro
 */
public record HotelRequest(
        
    @NotBlank(message = "El nombre del hotel no puede estar vacío")
    @Size(max = 100, message = "El nombre no puede exceder los 100 caracteres")
    String nombre,

    @NotBlank(message = "La dirección no puede estar vacía")
    @Size(max = 150, message = "La dirección no puede exceder los 150 caracteres")
    String direccion,

    @NotNull(message = "El ID de la ciudad es obligatorio")
    Integer idCiudad,

    @NotBlank(message = "El teléfono no puede estar vacío")
    @Size(max = 30, message = "El teléfono no puede exceder los 30 caracteres")
    String telefono,

    @NotNull(message = "La cantidad de plazas totales es obligatoria")
    @Min(value = 0, message = "Las plazas totales no pueden ser un valor negativo")
    Integer plazasTotales
) {}

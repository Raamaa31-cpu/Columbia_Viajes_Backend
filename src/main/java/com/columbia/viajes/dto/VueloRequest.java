/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.columbia.viajes.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 *
 * @author Ramiro
 */
public record VueloRequest(
        @NotNull(message = "La id de la ciudad de origen no puede estar vacia")
        Integer idCiudadOrigen,
        
        @NotNull(message = "La id de la ciudad de destino no puede estar vacia")
        Integer idCiudadDestino,
        
        @NotNull(message = "La fecha de salida no puede estar vacia")
        @Future(message = "La fecha de salida debe ser futura")
        LocalDateTime fechaSalida,
        
        @NotNull(message = "La fecha de llegada no puede estar vacia")
        LocalDateTime fechaLlegada,
        
        @NotNull(message = "Las plazas totales de clase turista no pueden ser un valor vacio")
        @Min(value = 0, message = "Las plazas totales de clase turista no pueden ser un valor negativo")
        Integer plazasTuristaTotales,
        
        @NotNull(message = "Las plazas totales de primera clase no pueden ser un valor vacio")
        @Min(value = 0, message = "Las plazas totales de primera clase no pueden ser un valor negativo")        
        Integer plazasPrimeraTotales
) {}

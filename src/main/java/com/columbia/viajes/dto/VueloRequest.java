/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.columbia.viajes.dto;

import java.time.LocalDateTime;

/**
 *
 * @author Ramiro
 */
public record VueloRequest(
        Integer idCiudadOrigen,
        Integer idCiudadDestino,
        LocalDateTime fechaSalida,
        LocalDateTime fechallegada,
        Integer plazasTuristaTotales,
        Integer plazasPrimeraTotales
) {}

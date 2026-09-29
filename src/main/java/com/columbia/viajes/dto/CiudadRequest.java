/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Record.java to edit this template
 */
package com.columbia.viajes.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 *
 * @author Ramiro
 */
public record CiudadRequest(
        @NotBlank(message = "El nombre de la ciudad no puede estar vacio")
        @Size(max = 100, message = "El nombre no puede exceder los 100 caracteres")
        String nombre
) {}

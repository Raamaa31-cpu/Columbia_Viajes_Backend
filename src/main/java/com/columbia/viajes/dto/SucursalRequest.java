/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Record.java to edit this template
 */
package com.columbia.viajes.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 *
 * @author Ramiro
 */
public record SucursalRequest(
        @NotBlank(message = "La direccion de la sucursal no puede estar vacia")
        @Size(max = 150, message = "La direccion no puede exceder los 150 caracteres")
        String direccion,
        
        @NotBlank(message = "El email no puede estar vacio")
        @Size(max = 120, message = "El email no puede exceder los 120 caracteres")
        @Email(message = "Debe ingresar una direccion de correo electronico valida")
        String email,
        
        @NotBlank(message = "El telefono no puede estar vacio")
        @Size(max = 30, message = "El telefono no puede exceder los 30 caracteres")
        String telefono
) {}

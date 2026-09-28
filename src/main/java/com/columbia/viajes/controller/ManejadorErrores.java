/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.columbia.viajes.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 *
 * @author Ramiro
 */

@RestControllerAdvice
public class ManejadorErrores {
    
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> datosInvalidos(IllegalArgumentException e){
        return ResponseEntity.badRequest().body(e.getMessage());
    }
    
}

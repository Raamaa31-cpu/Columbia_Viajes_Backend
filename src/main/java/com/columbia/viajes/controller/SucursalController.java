/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.columbia.viajes.controller;

/**
 *
 * @author Ramiro
 */

import com.columbia.viajes.dto.SucursalRequest;
import com.columbia.viajes.model.Sucursal;
import com.columbia.viajes.service.SucursalService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sucursales")
@RequiredArgsConstructor
public class SucursalController {
    
    private final SucursalService sucursalService;
    
    @GetMapping
    public ResponseEntity<List<Sucursal>> listar(){
        return ResponseEntity.ok(sucursalService.listar());
    } 
    
    @GetMapping("/{id}")
    public ResponseEntity<Sucursal> obtener(@PathVariable Integer id){
        return sucursalService.obtener(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping
    public ResponseEntity<Sucursal> crear(@Valid @RequestBody SucursalRequest request){
        Sucursal sucursalNueva = sucursalService.crear(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(sucursalNueva);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Sucursal> actualizar(@PathVariable Integer id, @Valid @RequestBody SucursalRequest request){
        return sucursalService.actualizar(id, request)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id){
        sucursalService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
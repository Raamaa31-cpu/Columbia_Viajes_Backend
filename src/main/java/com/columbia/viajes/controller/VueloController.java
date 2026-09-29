/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.columbia.viajes.controller;

import com.columbia.viajes.dto.VueloRequest;
import com.columbia.viajes.model.Vuelo;
import com.columbia.viajes.service.VueloService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author Ramiro
 */

@RestController
@RequestMapping("/api/vuelos")
@RequiredArgsConstructor
public class VueloController {
    
    private final VueloService vueloService;
    
    @GetMapping
    public List<Vuelo> listar(){
        return vueloService.listar();
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Vuelo> obtener(@PathVariable Integer id){
        return vueloService.obtener(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping
    public Vuelo crear(@RequestBody VueloRequest request){
        return vueloService.crear(request);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Vuelo> actualizar(@PathVariable Integer id, @RequestBody VueloRequest request){
        return vueloService.actualizar(id, request)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id){
        return vueloService.eliminar(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }
    
}

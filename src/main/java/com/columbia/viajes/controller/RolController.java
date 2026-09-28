/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.columbia.viajes.controller;

import com.columbia.viajes.model.Rol;
import com.columbia.viajes.service.RolService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author Ramiro
 */

@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
public class RolController {
    
    private final RolService rolService;
    
    @GetMapping
    public List<Rol> listar(){
        return rolService.listar();
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Rol> obtener(@PathVariable Integer id){
        return rolService.obtener(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}

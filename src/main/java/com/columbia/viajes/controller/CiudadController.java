package com.columbia.viajes.controller;

import com.columbia.viajes.model.Ciudad;
import com.columbia.viajes.service.CiudadService;
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

@RestController
@RequestMapping("/api/ciudades")
@RequiredArgsConstructor
public class CiudadController {
    
    private final CiudadService ciudadService;
    
    @GetMapping
    public List<Ciudad> listar(){
        return ciudadService.listar();
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Ciudad> obtener(@PathVariable Integer id){
        return ciudadService.obtener(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping
    public Ciudad crear(@RequestBody Ciudad ciudad){
        return ciudadService.crear(ciudad);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Ciudad> actualizar(@PathVariable Integer id, @RequestBody Ciudad datos){
        return ciudadService.actualizar(id, datos)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id){
        return ciudadService.eliminar(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }
    
}

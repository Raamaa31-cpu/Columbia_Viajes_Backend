package com.columbia.viajes.controller;

import com.columbia.viajes.dto.request.CiudadRequest;
import com.columbia.viajes.dto.response.CiudadResponse;
import com.columbia.viajes.service.CiudadService;
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
@RequestMapping("/api/ciudades")
@RequiredArgsConstructor
public class CiudadController {
    
    private final CiudadService ciudadService;
    
    @GetMapping
    public ResponseEntity<List<CiudadResponse>> listar(){
        return ResponseEntity.ok(ciudadService.listar());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<CiudadResponse> obtener(@PathVariable Integer id){
        return ciudadService.obtener(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping
    public ResponseEntity<CiudadResponse> crear(@Valid @RequestBody CiudadRequest request){
        CiudadResponse ciudadNueva = ciudadService.crear(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ciudadNueva);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<CiudadResponse> actualizar(@PathVariable Integer id, @Valid @RequestBody CiudadRequest request){
        return ciudadService.actualizar(id, request)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id){
        ciudadService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
    
}

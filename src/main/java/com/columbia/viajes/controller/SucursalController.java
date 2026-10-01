package com.columbia.viajes.controller;

import com.columbia.viajes.dto.request.SucursalRequest;
import com.columbia.viajes.dto.response.SucursalResponse;
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
    public ResponseEntity<List<SucursalResponse>> listar(){
        return ResponseEntity.ok(sucursalService.listar());
    } 
    
    @GetMapping("/{id}")
    public ResponseEntity<SucursalResponse> obtener(@PathVariable Integer id){
        return sucursalService.obtener(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping
    public ResponseEntity<SucursalResponse> crear(@Valid @RequestBody SucursalRequest request){
        SucursalResponse sucursalNueva = sucursalService.crear(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(sucursalNueva);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<SucursalResponse> actualizar(@PathVariable Integer id, @Valid @RequestBody SucursalRequest request){
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
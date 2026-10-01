package com.columbia.viajes.controller;

import com.columbia.viajes.dto.request.VueloRequest;
import com.columbia.viajes.dto.response.VueloResponse;
import com.columbia.viajes.service.VueloService;
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
@RequestMapping("/api/vuelos")
@RequiredArgsConstructor
public class VueloController {
    
    private final VueloService vueloService;
    
    @GetMapping
    public ResponseEntity<List<VueloResponse>> listar(){
        return ResponseEntity.ok(vueloService.listar());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<VueloResponse> obtener(@PathVariable Integer id){
        return vueloService.obtener(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping
    public ResponseEntity<VueloResponse> crear(@Valid @RequestBody VueloRequest request){
        VueloResponse vueloNuevo = vueloService.crear(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(vueloNuevo);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<VueloResponse> actualizar(@PathVariable Integer id, @Valid @RequestBody VueloRequest request){
        return vueloService.actualizar(id, request)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id){
        vueloService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
    
}

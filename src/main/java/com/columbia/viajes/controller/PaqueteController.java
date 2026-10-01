package com.columbia.viajes.controller;

import com.columbia.viajes.dto.request.PaqueteRequest;
import com.columbia.viajes.dto.response.PaqueteResponse;
import com.columbia.viajes.service.PaqueteService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/paquetes")
@RequiredArgsConstructor
public class PaqueteController {

    private final PaqueteService paqueteService;

    @GetMapping
    public ResponseEntity<List<PaqueteResponse>> listar() {
        return ResponseEntity.ok(paqueteService.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaqueteResponse> obtener(@PathVariable Integer id) {
        return paqueteService.obtener(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<PaqueteResponse> crear(@Valid @RequestBody PaqueteRequest request) {
        PaqueteResponse nuevoPaquete = paqueteService.crear(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevoPaquete);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PaqueteResponse> actualizar(@PathVariable Integer id, @Valid @RequestBody PaqueteRequest request) {
        return paqueteService.actualizar(id, request)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        paqueteService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}

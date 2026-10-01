package com.columbia.viajes.controller;

import com.columbia.viajes.dto.request.TuristaRequest;
import com.columbia.viajes.dto.response.TuristaResponse;
import com.columbia.viajes.service.TuristaService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/turistas")
@RequiredArgsConstructor
public class TuristaController {

    private final TuristaService turistaService;

    @GetMapping
    public ResponseEntity<List<TuristaResponse>> listar() {
        return ResponseEntity.ok(turistaService.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TuristaResponse> obtener(@PathVariable Integer id) {
        return turistaService.obtener(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<TuristaResponse> crear(@Valid @RequestBody TuristaRequest request) {
        TuristaResponse nuevoTurista = turistaService.crear(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevoTurista);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TuristaResponse> actualizar(@PathVariable Integer id, @Valid @RequestBody TuristaRequest request) {
        return turistaService.actualizar(id, request)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        turistaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}

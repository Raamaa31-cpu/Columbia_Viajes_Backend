package com.columbia.viajes.controller;

import com.columbia.viajes.dto.request.PaqueteReservaRequest;
import com.columbia.viajes.dto.response.PaqueteReservaResponse;
import com.columbia.viajes.service.PaqueteReservaService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/paquetes-reservas")
@RequiredArgsConstructor
public class PaqueteReservaController {

    private final PaqueteReservaService paqueteReservaService;

    @GetMapping
    public ResponseEntity<List<PaqueteReservaResponse>> listar() {
        return ResponseEntity.ok(paqueteReservaService.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaqueteReservaResponse> obtener(@PathVariable Integer id) {
        return paqueteReservaService.obtener(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<PaqueteReservaResponse> crear(@Valid @RequestBody PaqueteReservaRequest request) {
        PaqueteReservaResponse nuevaReserva = paqueteReservaService.crear(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevaReserva);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PaqueteReservaResponse> actualizar(@PathVariable Integer id, @Valid @RequestBody PaqueteReservaRequest request) {
        return paqueteReservaService.actualizar(id, request)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        paqueteReservaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}

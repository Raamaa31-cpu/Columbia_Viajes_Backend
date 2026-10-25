package com.columbia.viajes.controller;

import com.columbia.viajes.dto.request.PaqueteRequest;
import com.columbia.viajes.dto.response.PaqueteResponse;
import com.columbia.viajes.service.PaqueteService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.CacheControl;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/paquetes")
@RequiredArgsConstructor
public class PaqueteController {

    private final PaqueteService paqueteService;

    @GetMapping
    public ResponseEntity<List<PaqueteResponse>> listar(
            @RequestParam(name = "nombre", required = false) String nombre,
            @RequestParam(name = "idCiudad", required = false) Integer idCiudad,
            @RequestParam(name = "idHotel", required = false) Integer idHotel) {
        return ResponseEntity.ok(paqueteService.listar(nombre, idCiudad, idHotel));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaqueteResponse> obtener(@PathVariable Integer id) {
        return paqueteService.obtener(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PaqueteResponse> crear(
            @Valid @RequestPart("paquete") PaqueteRequest request,
            @RequestPart(value = "imagen", required = false) MultipartFile imagen) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(paqueteService.crear(request, imagen));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PaqueteResponse> actualizar(@PathVariable Integer id, @Valid @RequestBody PaqueteRequest request) {
        return paqueteService.actualizar(id, request)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping(value = "/{id}/imagen", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PaqueteResponse> actualizarImagen(
            @PathVariable Integer id,
            @RequestParam("imagen") MultipartFile imagen) {
        return ResponseEntity.ok(paqueteService.actualizarImagen(id, imagen));
    }

    @GetMapping(value = "/{id}/imagen", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<Resource> obtenerImagen(@PathVariable Integer id) {
        Resource imagen = paqueteService.obtenerImagen(id);
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .cacheControl(CacheControl.noCache())
                .eTag(imagen.getFilename())
                .body(imagen);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        paqueteService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}

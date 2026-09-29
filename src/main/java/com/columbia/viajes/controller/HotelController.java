/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.columbia.viajes.controller;

import com.columbia.viajes.dto.HotelRequest;
import com.columbia.viajes.model.Hotel;
import com.columbia.viajes.service.HotelService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 *
 * @author Ramiro
 */
@RestController
@RequestMapping("/api/hoteles")
@RequiredArgsConstructor
public class HotelController {

    private final HotelService hotelService;

    @GetMapping
    public ResponseEntity<List<Hotel>> listar() {
        return ResponseEntity.ok(hotelService.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Hotel> obtener(@PathVariable Integer id) {
        return hotelService.obtener(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Hotel> crear(@Valid @RequestBody HotelRequest request) {
        Hotel nuevoHotel = hotelService.crear(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevoHotel);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Hotel> actualizar(@PathVariable Integer id, @Valid @RequestBody HotelRequest request) {
        return hotelService.actualizar(id, request)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        hotelService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}

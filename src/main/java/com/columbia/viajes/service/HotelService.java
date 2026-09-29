/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.columbia.viajes.service;

import com.columbia.viajes.dto.HotelRequest;
import com.columbia.viajes.model.Ciudad;
import com.columbia.viajes.model.Hotel;
import com.columbia.viajes.repository.CiudadRepository;
import com.columbia.viajes.repository.HotelRepository;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 *
 * @author Ramiro
 */

@Service
@RequiredArgsConstructor
public class HotelService {
    private final HotelRepository hotelRepository;
    private final CiudadRepository ciudadRepository;

    public List<Hotel> listar() {
        return hotelRepository.findAll();
    }

    public Optional<Hotel> obtener(Integer id) {
        return hotelRepository.findById(id);
    }

    @Transactional
    public Hotel crear(HotelRequest request) {
        Ciudad ciudad = ciudadRepository.findById(request.idCiudad())
                .orElseThrow(() -> new IllegalArgumentException("La ciudad con ID " + request.idCiudad() + " no existe."));

        Hotel nuevoHotel = new Hotel();
        nuevoHotel.setNombre(request.nombre());
        nuevoHotel.setDireccion(request.direccion());
        nuevoHotel.setTelefono(request.telefono());
        nuevoHotel.setPlazasTotales(request.plazasTotales());
        nuevoHotel.setCiudad(ciudad);

        return hotelRepository.save(nuevoHotel);
    }

    @Transactional
    public Optional<Hotel> actualizar(Integer id, HotelRequest request) {
        return hotelRepository.findById(id)
                .map(hotelExistente -> {
                    Ciudad ciudad = ciudadRepository.findById(request.idCiudad())
                            .orElseThrow(() -> new IllegalArgumentException("La ciudad con ID " + request.idCiudad() + " no existe."));

                    hotelExistente.setNombre(request.nombre());
                    hotelExistente.setDireccion(request.direccion());
                    hotelExistente.setTelefono(request.telefono());
                    hotelExistente.setPlazasTotales(request.plazasTotales());
                    hotelExistente.setCiudad(ciudad);

                    return hotelRepository.save(hotelExistente);
                });
    }

    @Transactional
    public void eliminar(Integer id) {
        Hotel hotel = hotelRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró el hotel con ID " + id));

        hotelRepository.delete(hotel);
    }
}

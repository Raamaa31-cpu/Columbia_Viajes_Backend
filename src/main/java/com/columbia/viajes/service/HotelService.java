package com.columbia.viajes.service;

import com.columbia.viajes.dto.request.HotelRequest;
import com.columbia.viajes.dto.response.HotelResponse;
import com.columbia.viajes.mapper.HotelMapper;
import com.columbia.viajes.model.Ciudad;
import com.columbia.viajes.model.Hotel;
import com.columbia.viajes.repository.CiudadRepository;
import com.columbia.viajes.repository.HotelRepository;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class HotelService {
    private final HotelRepository hotelRepository;
    private final CiudadRepository ciudadRepository;
    
    private final HotelMapper hotelMapper;

    public List<HotelResponse> listar() {
        return hotelRepository.findAll()
                .stream()
                .map(hotelMapper::toHotelResponse)
                .toList();
    }

    public Optional<HotelResponse> obtener(Integer id) {
        return hotelRepository.findById(id)
                .map(hotelMapper::toHotelResponse);
    }

    @Transactional
    public HotelResponse crear(HotelRequest request) {
        Ciudad ciudad = ciudadRepository.findById(request.idCiudad())
                .orElseThrow(() -> new IllegalArgumentException("La ciudad con ID " + request.idCiudad() + " no existe."));

        Hotel hotelNuevo = hotelMapper.toHotel(request, ciudad);

        Hotel hotelGuardado = hotelRepository.save(hotelNuevo);
        return hotelMapper.toHotelResponse(hotelGuardado);
    }

    @Transactional
    public Optional<HotelResponse> actualizar(Integer id, HotelRequest request) {
        return hotelRepository.findById(id)
                .map(h -> {
                    Ciudad ciudad = ciudadRepository.findById(request.idCiudad())
                            .orElseThrow(() -> new IllegalArgumentException("La ciudad con ID " + request.idCiudad() + " no existe."));
                    
                    hotelMapper.actualizarHotel(request, ciudad, h);

                    Hotel hotelActualizado = hotelRepository.save(h);
                    return hotelMapper.toHotelResponse(hotelActualizado);
                });
    }

    @Transactional
    public void eliminar(Integer id) {
        Hotel hotel = hotelRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró el hotel con ID " + id));

        hotelRepository.delete(hotel);
    }
}

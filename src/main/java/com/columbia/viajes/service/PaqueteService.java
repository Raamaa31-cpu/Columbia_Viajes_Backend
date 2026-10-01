package com.columbia.viajes.service;

import com.columbia.viajes.dto.request.PaqueteRequest;
import com.columbia.viajes.dto.response.PaqueteResponse;
import com.columbia.viajes.mapper.PaqueteMapper;
import com.columbia.viajes.model.Hotel;
import com.columbia.viajes.model.Paquete;
import com.columbia.viajes.model.Sucursal;
import com.columbia.viajes.model.Vuelo;
import com.columbia.viajes.repository.HotelRepository;
import com.columbia.viajes.repository.PaqueteRepository;
import com.columbia.viajes.repository.SucursalRepository;
import com.columbia.viajes.repository.VueloRepository;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaqueteService {

    private final PaqueteRepository paqueteRepository;
    private final SucursalRepository sucursalRepository;
    private final VueloRepository vueloRepository;
    private final HotelRepository hotelRepository;
    private final PaqueteMapper paqueteMapper;

    public List<PaqueteResponse> listar() {
        return paqueteRepository.findAll()
                .stream()
                .map(paqueteMapper::toPaqueteResponse)
                .toList();
    }

    public Optional<PaqueteResponse> obtener(Integer id) {
        return paqueteRepository.findById(id)
                .map(paqueteMapper::toPaqueteResponse);
    }

    @Transactional
    public PaqueteResponse crear(PaqueteRequest request) {
        Sucursal sucursal = sucursalRepository.findById(request.idSucursal())
                .orElseThrow(() -> new IllegalArgumentException("La sucursal con ID " + request.idSucursal() + " no existe."));

        Vuelo vueloIda = vueloRepository.findById(request.idVueloIda())
                .orElseThrow(() -> new IllegalArgumentException("El vuelo con ID " + request.idVueloIda() + " no existe."));

        Vuelo vueloVuelta = vueloRepository.findById(request.idVueloVuelta())
                .orElseThrow(() -> new IllegalArgumentException("El vuelo con ID " + request.idVueloVuelta() + " no existe."));

        Hotel hotel = hotelRepository.findById(request.idHotel())
                .orElseThrow(() -> new IllegalArgumentException("El hotel con ID " + request.idHotel() + " no existe."));

        validarReglasDePaquete(request);

        Paquete paqueteNuevo = paqueteMapper.toPaquete(request, sucursal, vueloIda, vueloVuelta, hotel);
        Paquete paqueteGuardado = paqueteRepository.save(paqueteNuevo);
        return paqueteMapper.toPaqueteResponse(paqueteGuardado);
    }

    @Transactional
    public Optional<PaqueteResponse> actualizar(Integer id, PaqueteRequest request) {
        return paqueteRepository.findById(id)
                .map(paquete -> {
                    Sucursal sucursal = sucursalRepository.findById(request.idSucursal())
                            .orElseThrow(() -> new IllegalArgumentException("La sucursal con ID " + request.idSucursal() + " no existe."));

                    Vuelo vueloIda = vueloRepository.findById(request.idVueloIda())
                            .orElseThrow(() -> new IllegalArgumentException("El vuelo con ID " + request.idVueloIda() + " no existe."));

                    Vuelo vueloVuelta = vueloRepository.findById(request.idVueloVuelta())
                            .orElseThrow(() -> new IllegalArgumentException("El vuelo con ID " + request.idVueloVuelta() + " no existe."));

                    Hotel hotel = hotelRepository.findById(request.idHotel())
                            .orElseThrow(() -> new IllegalArgumentException("El hotel con ID " + request.idHotel() + " no existe."));
                    
                    validarReglasDePaquete(request);

                    paqueteMapper.actualizarPaquete(request, sucursal, vueloIda, vueloVuelta, hotel, paquete);

                    Paquete paqueteActualizado = paqueteRepository.save(paquete);
                    return paqueteMapper.toPaqueteResponse(paqueteActualizado);
                });
    }

    @Transactional
    public void eliminar(Integer id) {
        Paquete paquete = paqueteRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró el paquete con ID " + id));

        paqueteRepository.delete(paquete);
    }
    
    private void validarReglasDePaquete(PaqueteRequest request){
        // El CHECK de la base de datos requiere: fecha_partida_hotel > fecha_llegada_hotel
        if (!request.fechaPartidaHotel().isAfter(request.fechaLlegadaHotel())) {
            throw new IllegalArgumentException("La fecha de partida del hotel debe ser posterior a la fecha de llegada.");
        }
    }
}

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
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaqueteService {

    private final PaqueteRepository paqueteRepository;
    private final SucursalRepository sucursalRepository;
    private final VueloRepository vueloRepository;
    private final HotelRepository hotelRepository;
    private final PaqueteMapper paqueteMapper;
    private final ImagenService imagenService;

    public List<PaqueteResponse> listar(String nombre, Integer idCiudad, Integer idHotel) {
        nombre = nombre != null ? nombre.strip() : nombre;
        
        return paqueteRepository.buscar(nombre, idCiudad, idHotel)
                .stream()
                .map(paqueteMapper::toPaqueteResponse)
                .toList();
    }

    public Optional<PaqueteResponse> obtener(Integer id) {
        return paqueteRepository.findById(id)
                .map(paqueteMapper::toPaqueteResponse);
    }

    public Resource obtenerImagen(Integer id) {
        Paquete paquete = paqueteRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "No se encontró el paquete."
                ));
        return imagenService.obtener(paquete.getImagen());
    }

    @Transactional
    public PaqueteResponse actualizarImagen(Integer id, MultipartFile archivo) {
        Paquete paquete = paqueteRepository.findByIdParaActualizar(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "No se encontró el paquete."
                ));

        asignarImagen(paquete, archivo);
        return paqueteMapper.toPaqueteResponse(paqueteRepository.save(paquete));
    }

    @Transactional
    public PaqueteResponse crear(PaqueteRequest request, MultipartFile imagen) {
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
        if (imagen != null) {
            asignarImagen(paqueteNuevo, imagen);
        }
        Paquete paqueteGuardado = paqueteRepository.save(paqueteNuevo);
        return paqueteMapper.toPaqueteResponse(paqueteGuardado);
    }

    @Transactional
    public Optional<PaqueteResponse> actualizar(Integer id, PaqueteRequest request) {
        return paqueteRepository.findByIdParaActualizar(id)
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
        Paquete paquete = paqueteRepository.findByIdParaActualizar(id)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró el paquete con ID " + id));

        paqueteRepository.delete(paquete);
        programarLimpiezaImagenes(paquete.getImagen(), null);
    }

    private void asignarImagen(Paquete paquete, MultipartFile archivo) {
        String anterior = paquete.getImagen();
        String nueva = imagenService.guardar(archivo);
        programarLimpiezaImagenes(anterior, nueva);
        paquete.setImagen(nueva);
    }

    private void programarLimpiezaImagenes(String anterior, String nueva) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int estado) {
                String nombre;
                if (estado == STATUS_COMMITTED) {
                    nombre = anterior;
                } else if (estado == STATUS_ROLLED_BACK) {
                    nombre = nueva;
                } else {
                    return;
                }

                if (nombre != null) {
                    try {
                        imagenService.eliminar(nombre);
                    } catch (RuntimeException e) {
                        log.warn("No se pudo limpiar el archivo de imagen {}.", nombre, e);
                    }
                }
            }
        });
    }
    
    private void validarReglasDePaquete(PaqueteRequest request){
        // El CHECK de la base de datos requiere: fecha_partida_hotel > fecha_llegada_hotel
        if (!request.fechaPartidaHotel().isAfter(request.fechaLlegadaHotel())) {
            throw new IllegalArgumentException("La fecha de partida del hotel debe ser posterior a la fecha de llegada.");
        }
    }
}

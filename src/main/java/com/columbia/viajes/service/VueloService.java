package com.columbia.viajes.service;

import com.columbia.viajes.dto.request.VueloRequest;
import com.columbia.viajes.dto.response.VueloResponse;
import com.columbia.viajes.mapper.VueloMapper;
import com.columbia.viajes.model.Ciudad;
import com.columbia.viajes.model.Vuelo;
import com.columbia.viajes.repository.CiudadRepository;
import com.columbia.viajes.repository.VueloRepository;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class VueloService {
    
    private final VueloRepository vueloRepository;
    private final CiudadRepository ciudadRepository;
    private final VueloMapper vueloMapper;
    
    public List<VueloResponse> listar(){
        return vueloRepository.findAll()
                .stream()
                .map(vueloMapper::toVueloResponse)
                .toList();
    }
    
    public Optional<VueloResponse> obtener(Integer id){
        return vueloRepository.findById(id)
                .map(vueloMapper::toVueloResponse);
    }
    
    @Transactional
    public VueloResponse crear(VueloRequest request){
        Ciudad origen = ciudadRepository.findById(request.idCiudadOrigen())
                .orElseThrow(() -> new IllegalArgumentException("La ciudad de origen con ID " + request.idCiudadOrigen() + " no existe"));
        
        Ciudad destino = ciudadRepository.findById(request.idCiudadDestino())
                .orElseThrow(() -> new IllegalArgumentException("La ciudad de destino con ID " + request.idCiudadDestino() + " no existe"));
        
        validarReglasDeVuelo(request, origen, destino);
        
        Vuelo vueloNuevo = vueloMapper.toVuelo(request, origen, destino);
        
        Vuelo vueloGuardado = vueloRepository.save(vueloNuevo);
        return vueloMapper.toVueloResponse(vueloGuardado);
    }
    
    @Transactional
    public Optional<VueloResponse> actualizar(Integer id, VueloRequest request){
        
        return vueloRepository.findById(id)
                .map(v -> {
                    
                    Ciudad origen = ciudadRepository.findById(request.idCiudadOrigen())
                            .orElseThrow(() -> new IllegalArgumentException("La ciudad de origen con ID " + request.idCiudadOrigen() + " no existe"));

                    Ciudad destino = ciudadRepository.findById(request.idCiudadDestino())
                            .orElseThrow(() -> new IllegalArgumentException("La ciudad de destino con ID " + request.idCiudadDestino() + " no existe"));

                    validarReglasDeVuelo(request, origen, destino);
                    
                    vueloMapper.actualizarVuelo(request, origen, destino, v);
                    
                    Vuelo vueloActualizado = vueloRepository.save(v);
                    return vueloMapper.toVueloResponse(vueloActualizado);
                });
    }
    
    @Transactional
    public void eliminar(Integer id){
        Vuelo vuelo = vueloRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No existe un vuelo con la ID: " + id));
        
        vueloRepository.delete(vuelo);
    }
    
    private void validarReglasDeVuelo(VueloRequest request, Ciudad origen, Ciudad destino){
        
        if(origen.getId().equals(destino.getId())){
            throw new IllegalArgumentException("Las ciudades de origen y destino no pueden ser las mismas");
        }
        
        if(!request.fechaLlegada().isAfter(request.fechaSalida())){
            throw new IllegalArgumentException("La fecha de llegada debe ser posterior a la salida");
        }
        
        if(request.fechaSalida().isBefore(LocalDateTime.now())){
            throw new IllegalArgumentException("La fecha de salida no puede ser en el pasado");
        }
        
        if(request.plazasPrimeraTotales() < 0 || request.plazasTuristaTotales() < 0){
            throw new IllegalArgumentException("Las plazas totales no pueden ser negativas");
        }
    }
    
}

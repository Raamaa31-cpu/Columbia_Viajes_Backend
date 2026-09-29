/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.columbia.viajes.service;

import com.columbia.viajes.dto.VueloRequest;
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

/**
 *
 * @author Ramiro
 */

@Service
@RequiredArgsConstructor
public class VueloService {
    
    private final VueloRepository vueloRepository;
    private final CiudadRepository ciudadRepository;
    
    public List<Vuelo> listar(){
        return vueloRepository.findAll();
    }
    
    public Optional<Vuelo> obtener(Integer id){
        return vueloRepository.findById(id);
    }
    
    @Transactional
    public Vuelo crear(VueloRequest request){
        Ciudad origen = ciudadRepository.findById(request.idCiudadOrigen())
                .orElseThrow(() -> new IllegalArgumentException("La ciudad de origen con ID " + request.idCiudadOrigen() + " no existe"));
        
        Ciudad destino = ciudadRepository.findById(request.idCiudadDestino())
                .orElseThrow(() -> new IllegalArgumentException("La ciudad de destino con ID " + request.idCiudadDestino() + " no existe"));
        
        validarReglasDeVuelo(request, origen, destino);
        
        Vuelo vueloNuevo = new Vuelo();
        vueloNuevo.setCiudadOrigen(origen);
        vueloNuevo.setCiudadDestino(destino);
        vueloNuevo.setFechaSalida(request.fechaSalida());
        vueloNuevo.setFechaLlegada(request.fechaLlegada());
        vueloNuevo.setPlazasTuristaTotales(request.plazasTuristaTotales());
        vueloNuevo.setPlazasPrimeraTotales(request.plazasPrimeraTotales());

        return vueloRepository.save(vueloNuevo);
    }
    
    @Transactional
    public Optional<Vuelo> actualizar(Integer id, VueloRequest request){
        
        return vueloRepository.findById(id)
                .map(v -> {
                    
                    Ciudad origen = ciudadRepository.findById(request.idCiudadOrigen())
                            .orElseThrow(() -> new IllegalArgumentException("La ciudad de origen con ID " + request.idCiudadOrigen() + " no existe"));

                    Ciudad destino = ciudadRepository.findById(request.idCiudadDestino())
                            .orElseThrow(() -> new IllegalArgumentException("La ciudad de destino con ID " + request.idCiudadDestino() + " no existe"));

                    validarReglasDeVuelo(request, origen, destino);
                    
                    v.setCiudadOrigen(origen);
                    v.setCiudadDestino(destino);
                    v.setFechaSalida(request.fechaSalida());
                    v.setFechaLlegada(request.fechaLlegada());
                    v.setPlazasTuristaTotales(request.plazasTuristaTotales());
                    v.setPlazasPrimeraTotales(request.plazasPrimeraTotales());
                    
                    return vueloRepository.save(v);
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

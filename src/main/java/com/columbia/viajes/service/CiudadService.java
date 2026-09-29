/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.columbia.viajes.service;

import com.columbia.viajes.dto.CiudadRequest;
import com.columbia.viajes.model.Ciudad;
import com.columbia.viajes.repository.CiudadRepository;
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
public class CiudadService {
    
    private final CiudadRepository ciudadRepository;
    
    public List<Ciudad> listar(){
        return ciudadRepository.findAll();
    }
    
    public Optional<Ciudad> obtener(Integer id){
        return ciudadRepository.findById(id);
    }
    
    @Transactional
    public Ciudad crear(CiudadRequest request){
        
        if(ciudadRepository.existsByNombre(request.nombre())){
            throw new IllegalArgumentException("Ya existe una ciudad registrada con el nombre: " + request.nombre());
        }
        
        Ciudad ciudadNueva = new Ciudad();
        ciudadNueva.setNombre(request.nombre());
        
        return ciudadRepository.save(ciudadNueva);
    }
    
    @Transactional
    public Optional<Ciudad> actualizar(Integer id, CiudadRequest request){
        return ciudadRepository.findById(id)
                .map(c -> {
                    if(!request.nombre().equals(c.getNombre()) && ciudadRepository.existsByNombre(request.nombre()) ){
                        throw new IllegalArgumentException("Ya existe una ciudad registrada con el nombre: " + request.nombre());
                    }
                    
                    c.setNombre(request.nombre());
                    
                    return ciudadRepository.save(c);
                });
    }
    
    @Transactional
    public void eliminar(Integer id){
        Ciudad ciudad = ciudadRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No se encontro la ciudad con ID: " + id));
        
        ciudadRepository.deleteById(id);
    }

}

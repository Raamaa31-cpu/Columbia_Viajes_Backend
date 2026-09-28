/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.columbia.viajes.service;

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
    public Ciudad crear(Ciudad ciudad){
        validar(ciudad);
        ciudad.setId(null);
        return ciudadRepository.save(ciudad);
    }
    
    @Transactional
    public Optional<Ciudad> actualizar(Integer id, Ciudad ciudad){
        validar(ciudad);
        return ciudadRepository.findById(id)
                .map(c -> {
                    c.setNombre(ciudad.getNombre());
                    
                    return ciudadRepository.save(c);
                });
    }
    
    @Transactional
    public boolean eliminar(Integer id){
        if(!ciudadRepository.existsById(id)){
            return false;
        }
        
        ciudadRepository.deleteById(id);
        return true;
    }
    
    private void validar(Ciudad ciudad){
        if(ciudad.getNombre() == null || ciudad.getNombre().isBlank()) {
            throw new IllegalArgumentException("El nombre de la ciudad es obligatorio");
        }
    }
}

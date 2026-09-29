/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.columbia.viajes.service;

import com.columbia.viajes.dto.SucursalRequest;
import com.columbia.viajes.model.Sucursal;
import com.columbia.viajes.repository.SucursalRepository;
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
public class SucursalService {
    
    private final SucursalRepository sucursalRepository;
    
    public List<Sucursal> listar(){
        return sucursalRepository.findAll();
    }
    
    public Optional<Sucursal> obtener(Integer id){
        return sucursalRepository.findById(id);
    }
    
    @Transactional
    public Sucursal crear(SucursalRequest request){
        Sucursal sucursalNueva = new Sucursal();
        sucursalNueva.setDireccion(request.direccion());
        sucursalNueva.setEmail(request.email());
        sucursalNueva.setTelefono(request.telefono());
        
        return sucursalRepository.save(sucursalNueva);
    }
    
    @Transactional
    public Optional<Sucursal> actualizar(Integer id, SucursalRequest request) {
        return sucursalRepository.findById(id)
                .map(s -> {
                    s.setDireccion(request.direccion());
                    s.setEmail(request.email());
                    s.setTelefono(request.telefono());
            
                    return sucursalRepository.save(s);
                });
    }

    @Transactional
    public void eliminar(Integer id) {
        Sucursal sucursal = sucursalRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No se encontro la sucursal con la ID: " + id));
                
        sucursalRepository.delete(sucursal);
    }
    
}

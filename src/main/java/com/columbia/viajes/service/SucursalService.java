/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.columbia.viajes.service;

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
    public Sucursal crear(Sucursal sucursal){
        sucursal.setId(null);
        return sucursalRepository.save(sucursal);
    }
    
    @Transactional
    public Optional<Sucursal> actualizar(Integer id, Sucursal datos) {
        validar(datos);
        return sucursalRepository.findById(id).map(s -> {
            s.setDireccion(datos.getDireccion());
            s.setEmail(datos.getEmail());
            s.setTelefono(datos.getTelefono());
            return sucursalRepository.save(s);
        });
    }

    @Transactional
    public boolean eliminar(Integer id) {
        if (!sucursalRepository.existsById(id)) {
            return false;
        }
        sucursalRepository.deleteById(id);
        return true;
    }
    
    private void validar(Sucursal sucursal){
        if(sucursal.getDireccion() == null || sucursal.getDireccion().isBlank()
                || sucursal.getEmail() == null || sucursal.getEmail().isBlank()
                || sucursal.getTelefono() == null || sucursal.getTelefono().isBlank()){
            throw new IllegalArgumentException("Direccion, email y telefono son obligatorios");
        }
    }
    
}

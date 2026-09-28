/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.columbia.viajes.service;

import com.columbia.viajes.model.Rol;
import com.columbia.viajes.repository.RolRepository;
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
public class RolService {

    private final RolRepository rolRepository;

    public List<Rol> listar() {
        return rolRepository.findAll();
    }

    public Optional<Rol> obtener(Integer id) {
        return rolRepository.findById(id);
    }
}

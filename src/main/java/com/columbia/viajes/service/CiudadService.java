package com.columbia.viajes.service;

import com.columbia.viajes.dto.request.CiudadRequest;
import com.columbia.viajes.dto.response.CiudadResponse;
import com.columbia.viajes.mapper.CiudadMapper;
import com.columbia.viajes.model.Ciudad;
import com.columbia.viajes.repository.CiudadRepository;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CiudadService {
    
    private final CiudadRepository ciudadRepository;
    private final CiudadMapper ciudadMapper;
    
    public List<CiudadResponse> listar(){
        return ciudadRepository.findAll()
                .stream()
                .map(ciudadMapper::toCiudadResponse)
                .toList();
    }
    
    public Optional<CiudadResponse> obtener(Integer id){
        return ciudadRepository.findById(id)
                .map(ciudadMapper::toCiudadResponse);
    }
    
    @Transactional
    public CiudadResponse crear(CiudadRequest request){
        
        if(ciudadRepository.existsByNombre(request.nombre())){
            throw new IllegalArgumentException("Ya existe una ciudad registrada con el nombre: " + request.nombre());
        }
        
        Ciudad ciudadNueva = ciudadMapper.toCiudad(request);
        
        Ciudad ciudadGuardada = ciudadRepository.save(ciudadNueva);
        return ciudadMapper.toCiudadResponse(ciudadGuardada);
    }
    
    @Transactional
    public Optional<CiudadResponse> actualizar(Integer id, CiudadRequest request){
        return ciudadRepository.findById(id)
                .map(c -> {
                    if(!request.nombre().equals(c.getNombre()) && ciudadRepository.existsByNombre(request.nombre()) ){
                        throw new IllegalArgumentException("Ya existe una ciudad registrada con el nombre: " + request.nombre());
                    }
                    
                    ciudadMapper.actualizarCiudad(request, c);
                    
                    Ciudad ciudadActualizada = ciudadRepository.save(c);
                    return ciudadMapper.toCiudadResponse(ciudadActualizada);
                });
    }
    
    @Transactional
    public void eliminar(Integer id){
        Ciudad ciudad = ciudadRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No se encontro la ciudad con ID: " + id));
        
        ciudadRepository.deleteById(id);
    }

}

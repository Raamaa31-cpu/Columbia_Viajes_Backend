package com.columbia.viajes.service;

import com.columbia.viajes.dto.request.SucursalRequest;
import com.columbia.viajes.dto.response.SucursalResponse;
import com.columbia.viajes.mapper.SucursalMapper;
import com.columbia.viajes.model.Sucursal;
import com.columbia.viajes.repository.SucursalRepository;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SucursalService {
    
    private final SucursalRepository sucursalRepository;
    private final SucursalMapper sucursalMapper;
    
    public List<SucursalResponse> listar(){
        return sucursalRepository.findAll()
                .stream()
                .map(sucursalMapper::toSucursalResponse)
                .toList();
    }
    
    public Optional<SucursalResponse> obtener(Integer id){
        return sucursalRepository.findById(id)
                .map(sucursalMapper::toSucursalResponse);
    }
    
    @Transactional
    public SucursalResponse crear(SucursalRequest request){
        
        Sucursal sucursalNueva = sucursalMapper.toSucursal(request);
        
        Sucursal sucursalGuardada = sucursalRepository.save(sucursalNueva);
        return sucursalMapper.toSucursalResponse(sucursalGuardada);
    }
    
    @Transactional
    public Optional<SucursalResponse> actualizar(Integer id, SucursalRequest request) {
        return sucursalRepository.findById(id)
                .map(s -> {
                    
                    sucursalMapper.actualizarSucursal(request, s);
                    
                    Sucursal sucursalActualizada = sucursalRepository.save(s);
                    return sucursalMapper.toSucursalResponse(sucursalActualizada);
                });
    }

    @Transactional
    public void eliminar(Integer id) {
        Sucursal sucursal = sucursalRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No se encontro la sucursal con la ID: " + id));
                
        sucursalRepository.delete(sucursal);
    }
    
}

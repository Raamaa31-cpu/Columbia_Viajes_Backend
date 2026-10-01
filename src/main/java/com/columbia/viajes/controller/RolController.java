package com.columbia.viajes.controller;

import com.columbia.viajes.dto.response.RolResponse;
import com.columbia.viajes.service.RolService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
public class RolController {
    
    private final RolService rolService;
    
    @GetMapping
    public List<RolResponse> listar(){
        return rolService.listar();
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<RolResponse> obtener(@PathVariable Integer id){
        return rolService.obtener(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}

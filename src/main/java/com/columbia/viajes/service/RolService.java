package com.columbia.viajes.service;

import com.columbia.viajes.dto.response.RolResponse;
import com.columbia.viajes.mapper.RolMapper;
import com.columbia.viajes.repository.RolRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RolService {

    private final RolRepository rolRepository;
    private final RolMapper rolMapper;

    public List<RolResponse> listar() {
        return rolRepository.findAll()
                .stream()
                .map(rolMapper::toRolResponse)
                .toList();
    }

    public Optional<RolResponse> obtener(Integer id) {
        return rolRepository.findById(id)
                .map(rolMapper::toRolResponse);
    }
}

package com.columbia.viajes.mapper;

import com.columbia.viajes.dto.request.CiudadRequest;
import com.columbia.viajes.dto.response.CiudadResponse;
import com.columbia.viajes.model.Ciudad;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface CiudadMapper {
    
    CiudadResponse toCiudadResponse(Ciudad ciudad);
    
    @Mapping(target = "id", ignore = true)
    Ciudad toCiudad(CiudadRequest request);
    
    @Mapping(target = "id", ignore = true)
    void actualizarCiudad(CiudadRequest request, @MappingTarget Ciudad ciudad);
    
}

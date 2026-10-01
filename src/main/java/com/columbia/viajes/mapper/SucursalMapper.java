package com.columbia.viajes.mapper;

import com.columbia.viajes.dto.request.SucursalRequest;
import com.columbia.viajes.dto.response.SucursalResponse;
import com.columbia.viajes.model.Sucursal;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface SucursalMapper {
    
    SucursalResponse toSucursalResponse(Sucursal sucursal);
    
    @Mapping(target = "id", ignore = true)
    Sucursal toSucursal(SucursalRequest request);
    
    @Mapping(target = "id", ignore = true)
    Sucursal actualizarSucursal(SucursalRequest request, @MappingTarget Sucursal sucursal);
    
}

package com.columbia.viajes.mapper;

import com.columbia.viajes.dto.request.HotelRequest;
import com.columbia.viajes.dto.response.HotelResponse;
import com.columbia.viajes.model.Ciudad;
import com.columbia.viajes.model.Hotel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface HotelMapper {

    @Mapping(source = "ciudad.nombre", target = "ciudadNombre")
    HotelResponse toHotelResponse(Hotel hotel);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "ciudad", source = "ciudad")
    @Mapping(target = "nombre", source = "request.nombre")
    Hotel toHotel(HotelRequest request, Ciudad ciudad);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "ciudad", source = "ciudad")
    @Mapping(target = "nombre", source = "request.nombre")
    void actualizarHotel(HotelRequest request, Ciudad ciudad, @MappingTarget Hotel hotel);
    
}

package com.columbia.viajes.mapper;

import com.columbia.viajes.dto.request.VueloRequest;
import com.columbia.viajes.dto.response.VueloResponse;
import com.columbia.viajes.model.Ciudad;
import com.columbia.viajes.model.Vuelo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface VueloMapper {

    @Mapping(source = "ciudadOrigen.nombre", target = "ciudadOrigenNombre")
    @Mapping(source = "ciudadDestino.nombre", target = "ciudadDestinoNombre")
    VueloResponse toVueloResponse(Vuelo vuelo);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "ciudadOrigen", source = "origen")
    @Mapping(target = "ciudadDestino", source = "destino")
    Vuelo toVuelo(VueloRequest request, Ciudad origen, Ciudad destino);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "ciudadOrigen", source = "origen")
    @Mapping(target = "ciudadDestino", source = "destino")
    void actualizarVuelo(VueloRequest request, Ciudad origen, Ciudad destino, @MappingTarget Vuelo vuelo);
    
}

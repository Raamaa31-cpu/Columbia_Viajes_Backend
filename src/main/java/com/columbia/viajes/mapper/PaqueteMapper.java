package com.columbia.viajes.mapper;

import com.columbia.viajes.dto.request.PaqueteRequest;
import com.columbia.viajes.dto.response.PaqueteResponse;
import com.columbia.viajes.model.Hotel;
import com.columbia.viajes.model.Paquete;
import com.columbia.viajes.model.Sucursal;
import com.columbia.viajes.model.Vuelo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface PaqueteMapper {

    @Mapping(source = "sucursal.id", target = "sucursalId")
    @Mapping(source = "vueloIda.id", target = "vueloIdaId")
    @Mapping(source = "vueloVuelta.id", target = "vueloVueltaId")
    @Mapping(source = "hotel.id", target = "hotelId")
    PaqueteResponse toPaqueteResponse(Paquete paquete);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "sucursal", source = "sucursal")
    @Mapping(target = "vueloIda", source = "vueloIda")
    @Mapping(target = "vueloVuelta", source = "vueloVuelta")
    @Mapping(target = "hotel", source = "hotel")
    @Mapping(target = "nombre", source = "request.nombre")
    @Mapping(target = "descripcion", source = "request.descripcion")
    @Mapping(target = "fechaLlegadaHotel", source = "request.fechaLlegadaHotel")
    @Mapping(target = "fechaPartidaHotel", source = "request.fechaPartidaHotel")
    @Mapping(target = "precio", source = "request.precio")
    @Mapping(target = "imagen", source = "request.imagen")
    Paquete toPaquete(PaqueteRequest request, Sucursal sucursal, Vuelo vueloIda, Vuelo vueloVuelta, Hotel hotel);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "sucursal", source = "sucursal")
    @Mapping(target = "vueloIda", source = "vueloIda")
    @Mapping(target = "vueloVuelta", source = "vueloVuelta")
    @Mapping(target = "hotel", source = "hotel")
    @Mapping(target = "nombre", source = "request.nombre")
    @Mapping(target = "descripcion", source = "request.descripcion")
    @Mapping(target = "fechaLlegadaHotel", source = "request.fechaLlegadaHotel")
    @Mapping(target = "fechaPartidaHotel", source = "request.fechaPartidaHotel")
    @Mapping(target = "precio", source = "request.precio")
    @Mapping(target = "imagen", source = "request.imagen")
    void actualizarPaquete(PaqueteRequest request, Sucursal sucursal, Vuelo vueloIda, Vuelo vueloVuelta, Hotel hotel, @MappingTarget Paquete paquete);

}

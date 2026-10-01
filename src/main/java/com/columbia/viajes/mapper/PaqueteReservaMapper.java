package com.columbia.viajes.mapper;

import com.columbia.viajes.dto.request.PaqueteReservaRequest;
import com.columbia.viajes.dto.response.PaqueteReservaResponse;
import com.columbia.viajes.model.Paquete;
import com.columbia.viajes.model.PaqueteReserva;
import com.columbia.viajes.model.Turista;
import com.columbia.viajes.model.Usuario;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface PaqueteReservaMapper {

    @Mapping(source = "paquete.id", target = "paqueteId")
    @Mapping(source = "turista.id", target = "turistaId")
    @Mapping(source = "vendedor.id", target = "vendedorId")
    PaqueteReservaResponse toPaqueteReservaResponse(PaqueteReserva paqueteReserva);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "fechaReserva", ignore = true)
    @Mapping(target = "paquete", source = "paquete")
    @Mapping(target = "turista", source = "turista")
    @Mapping(target = "vendedor", source = "vendedor")
    PaqueteReserva toPaqueteReserva(PaqueteReservaRequest request, Paquete paquete, Turista turista, Usuario vendedor);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "fechaReserva", ignore = true)
    @Mapping(target = "paquete", source = "paquete")
    @Mapping(target = "turista", source = "turista")
    @Mapping(target = "vendedor", source = "vendedor")
    void actualizarPaqueteReserva(PaqueteReservaRequest request, Paquete paquete, Turista turista, Usuario vendedor, @MappingTarget PaqueteReserva paqueteReserva);

}

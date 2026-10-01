package com.columbia.viajes.mapper;

import com.columbia.viajes.dto.request.TuristaRequest;
import com.columbia.viajes.dto.response.TuristaResponse;
import com.columbia.viajes.model.Turista;
import com.columbia.viajes.model.Usuario;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface TuristaMapper {

    @Mapping(source = "usuario.id", target = "usuarioId")
    TuristaResponse toTuristaResponse(Turista turista);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "usuario", source = "usuario")
    @Mapping(target = "nombre", source = "request.nombre")
    @Mapping(target = "apellidos", source = "request.apellidos")
    @Mapping(target = "direccion", source = "request.direccion")
    @Mapping(target = "email", source = "request.email")
    @Mapping(target = "telefono", source = "request.telefono")
    Turista toTurista(TuristaRequest request, Usuario usuario);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "usuario", source = "usuario")
    @Mapping(target = "nombre", source = "request.nombre")
    @Mapping(target = "apellidos", source = "request.apellidos")
    @Mapping(target = "direccion", source = "request.direccion")
    @Mapping(target = "email", source = "request.email")
    @Mapping(target = "telefono", source = "request.telefono")
    void actualizarTurista(TuristaRequest request, Usuario usuario, @MappingTarget Turista turista);

}

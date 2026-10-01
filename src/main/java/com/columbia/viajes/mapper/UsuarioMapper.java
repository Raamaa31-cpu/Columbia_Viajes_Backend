package com.columbia.viajes.mapper;

import com.columbia.viajes.dto.request.UsuarioRequest;
import com.columbia.viajes.dto.response.UsuarioResponse;
import com.columbia.viajes.model.Rol;
import com.columbia.viajes.model.Sucursal;
import com.columbia.viajes.model.Usuario;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UsuarioMapper {

    @Mapping(source = "rol.nombre", target = "rolNombre")
    UsuarioResponse toUsuarioResponse(Usuario usuario);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "rol", source = "rol")
    @Mapping(target = "nombre", source = "request.nombre")
    @Mapping(target = "contrasenia", source = "request.contrasenia")
    @Mapping(target = "sucursal", source = "sucursal")
    Usuario toUsuario(UsuarioRequest request, Rol rol, Sucursal sucursal);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "rol", source = "rol")
    @Mapping(target = "nombre", source = "request.nombre")
    @Mapping(target = "contrasenia", source = "request.contrasenia")
    @Mapping(target = "sucursal", source = "sucursal")
    void actualizarUsuario(UsuarioRequest request, Rol rol, Sucursal sucursal, @MappingTarget Usuario usuario);
    
}
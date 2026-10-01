/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.columbia.viajes.mapper;

import com.columbia.viajes.dto.response.RolResponse;
import com.columbia.viajes.model.Rol;
import org.mapstruct.Mapper;

/**
 *
 * @author Ramiro
 */

@Mapper(componentModel = "spring")
public interface RolMapper {
    
    RolResponse toRolResponse(Rol rol);
    
}

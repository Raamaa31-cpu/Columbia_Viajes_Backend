package com.columbia.viajes.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginResponse {
    private String mensaje;
    private String nombre;
    private Integer idRol;
    private String token;
}
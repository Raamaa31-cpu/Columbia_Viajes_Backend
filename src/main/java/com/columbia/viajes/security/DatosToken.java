package com.columbia.viajes.security;

public record DatosToken(
        String nombreUsuario,
        long versionAutenticacion
        
) {}

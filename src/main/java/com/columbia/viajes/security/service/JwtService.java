package com.columbia.viajes.security.service;

import com.columbia.viajes.security.DatosToken;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private final SecretKey clave;
    private final long duracionMs;
    
    public JwtService(@Value("${app.jwt.secret}") String secreto, @Value("${app.jwt.expiration-ms}") long duracionMs){
        
        this.clave = Keys.hmacShaKeyFor(
                Decoders.BASE64.decode(secreto)
        );
        
        if(duracionMs <= 0){
            throw new IllegalArgumentException(
                    "La duración del token debe ser mayor que cero."
            );
        }
        
        this.duracionMs = duracionMs;
    }
    
    public String generarToken(String nombreUsuario, long versionAutenticacion){
        
        Instant creacion = Instant.now();
        Instant vencimiento = creacion.plusMillis(duracionMs);
        
        return Jwts.builder()
                .subject(nombreUsuario)
                .claim("versionAutenticacion", versionAutenticacion)
                .issuedAt(Date.from(creacion))
                .expiration(Date.from(vencimiento))
                .signWith(clave, Jwts.SIG.HS256)
                .compact();
        
    }
    
    public DatosToken leerToken(String token){
        
        var contenido = Jwts.parser()
                .verifyWith(clave)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        
        String nombreUsuario = contenido.getSubject();
        Long versionAutenticacion = contenido.get("versionAutenticacion", Long.class);
        
        if(nombreUsuario == null || nombreUsuario.isBlank() || contenido.getExpiration() == null || versionAutenticacion == null || versionAutenticacion < 0){
            throw new JwtException("El token debe incluir usuario, vencimiento y versión de autenticación");
        }
        
        return new DatosToken(nombreUsuario, versionAutenticacion);
    }
    
}

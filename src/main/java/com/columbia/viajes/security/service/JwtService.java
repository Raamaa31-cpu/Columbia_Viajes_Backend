package com.columbia.viajes.security.service;

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
    
    public String generarToken(String nombreUsuario){
        
        Instant creacion = Instant.now();
        Instant vencimiento = creacion.plusMillis(duracionMs);
        
        return Jwts.builder()
                .subject(nombreUsuario)
                .issuedAt(Date.from(creacion))
                .expiration(Date.from(vencimiento))
                .signWith(clave, Jwts.SIG.HS256)
                .compact();
        
    }
    
    public String obtenerNombreUsuario(String token){
        
        var contenido = Jwts.parser()
                .verifyWith(clave)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        
        String nombreUsuario = contenido.getSubject();
        
        if(nombreUsuario == null || nombreUsuario.isBlank() || contenido.getExpiration() == null){
            throw new JwtException("El token debe incluir usuario y vencimiento");
        }
        
        return nombreUsuario;
    }
    
}

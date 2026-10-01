package com.columbia.viajes.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

/**
 * Utilitario para la generación y validación de tokens JWT.
 *
 * <p>Un JWT tiene 3 partes separadas por puntos:</p>
 * <ol>
 *   <li><b>Header</b>: algoritmo (HS256) y tipo (JWT).</li>
 *   <li><b>Payload</b>: claims (datos del usuario, expiración, etc.).</li>
 *   <li><b>Signature</b>: firma HMAC-SHA256 con la clave secreta.</li>
 * </ol>
 *
 * <p>Este bean es {@code @Component} (singleton) y se inyecta donde se necesita.</p>
 */
@Component
public class JwtUtil {

    // La clave secreta se carga desde application.properties (app.jwt.secret)
    private final String secret;
    // El tiempo de expiración en milisegundos (86400000 = 24 horas)
    private final long jwtExpirationMs;

    public JwtUtil(@Value("${app.jwt.secret}") String secret,
                   @Value("${app.jwt.expiration-ms}") long jwtExpirationMs) {
        this.secret = secret;
        this.jwtExpirationMs = jwtExpirationMs;
    }

    /**
     * Genera un token JWT para el usuario autenticado.
     *
     * @param userDetails los detalles del usuario (username + authorities)
     * @return el token JWT como String
     */
    public String generarToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        // Agregamos los roles/authorities como claim para poder usarlos después
        claims.put("roles", userDetails.getAuthorities());
        return construirToken(claims, userDetails.getUsername());
    }

    /**
     * Método privado que construye el token con la firma.
     */
    private String construirToken(Map<String, Object> claims, String subject) {
        Date ahora = new Date(System.currentTimeMillis());
        Date expiracion = new Date(System.currentTimeMillis() + jwtExpirationMs);

        return Jwts.builder()
                .setClaims(claims)           // datos extras (roles)
                .setSubject(subject)         // username (el "dueño" del token)
                .setIssuedAt(ahora)          // fecha de emisión
                .setExpiration(expiracion)   // fecha de expiración
                .signWith(getClaveFirma(), SignatureAlgorithm.HS256)  // firma HMAC-SHA256
                .compact();                  // serializa a String
    }

    /**
     * Extrae el username (subject) del token.
     */
    public String extraerUsername(String token) {
        return extraerClaim(token, Claims::getSubject);
    }

    /**
     * Extrae un claim específico del token usando una función.
     * (Ej: extraer expiración, subject, roles, etc.)
     */
    public <T> T extraerClaim(String token, Function<Claims, T> resolvedorClaims) {
        final Claims claims = parsearClaims(token);
        return resolvedorClaims.apply(claims);
    }

    /**
     * Parsea el token y extrae todos los claims (validando la firma en el proceso).
     * Lanza JwtException si el token es inválido o expirado.
     */
    private Claims parsearClaims(String token) {
        return Jwts.parser()
                .verifyWith(getClaveFirma())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Verifica si el token está expirado.
     */
    private boolean estaExpirado(String token) {
        Date expiracion = extraerClaim(token, Claims::getExpiration);
        return expiracion.before(new Date());
    }

    /**
     * Valida el token contra los detalles del usuario.
     * El token es válido si:
     * 1. El username coincide con el del UserDetails.
     * 2. El token no está expirado.
     */
    public boolean validarToken(String token, UserDetails userDetails) {
        final String username = extraerUsername(token);
        return (username.equals(userDetails.getUsername())) && !estaExpirado(token);
    }

    /**
     * Convierte la clave secreta (String) en un {@code SecretKey}
     * que JJWT puede usar para firmar/validar.
     */
    private SecretKey getClaveFirma() {
        byte[] keyBytes = secret.getBytes();
        return Keys.hmacShaKeyFor(keyBytes);
    }
}

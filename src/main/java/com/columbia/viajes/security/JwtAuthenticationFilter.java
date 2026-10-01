package com.columbia.viajes.security;

import com.columbia.viajes.security.service.UserDetailsServiceImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Filtro JWT que se ejecuta una sola vez por cada request.
 *
 * <p><b>Ciclo de vida:</b></p>
 * <ol>
 *   <li>El cliente envía una request con el header
 *       {@code Authorization: Bearer eyJhbGci...}.</li>
 *   <li>Este filtro intercepta la request <b>antes</b> de que llegue al controlador.</li>
 *   <li>Extrae el token del header, lo valida con {@link JwtUtil}.</li>
 *   <li>Si el token es válido, carga los datos del usuario desde la base de datos
 *       mediante {@link UserDetailsServiceImpl} y crea un objeto
 *       {@link UsernamePasswordAuthenticationToken}.</li>
 *   <li>Ese objeto se coloca en el {@link SecurityContextHolder}, lo que indica
 *       a Spring Security que "el usuario X está autenticado".</li>
 *   <li>La request continúa su camino hacia el controlador.</li>
 * </ol>
 *
 * <p>Extiende {@link OncePerRequestFilter} para garantizar que se ejecuta
 * exactamente una vez por request (no doble ejecución en casos de forward).</p>
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsServiceImpl userDetailsServiceImpl;

    public JwtAuthenticationFilter(JwtUtil jwtUtil, UserDetailsServiceImpl userDetailsServiceImpl) {
        this.jwtUtil = jwtUtil;
        this.userDetailsServiceImpl = userDetailsServiceImpl;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        // 1. Extraer el header "Authorization" de la request
        final String authHeader = request.getHeader("Authorization");
        String token = null;
        String username = null;

        // 2. El header debe tener la forma "Bearer <token>"
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7); // quita "Bearer " (7 caracteres)
            try {
                // 3. Extraer el username del token (valida la firma en el proceso)
                username = jwtUtil.extraerUsername(token);
            } catch (Exception e) {
                // Si el token es inválido o expiró, simplemente no autenticamos
                // (la request continuará sin SecurityContext y será rechazada por los filtros de autorización)
                logger.warn("Token JWT inválido o expirado: " + e.getMessage());
            }
        }

        // 4. Si tenemos un username y aún no hay autenticación en el contexto...
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            // 5. Cargar el usuario desde la base de datos
            UserDetails userDetails = userDetailsServiceImpl.loadUserByUsername(username);

            // 6. Validar que el token pertenece a ese usuario y no está expirado
            if (jwtUtil.validarToken(token, userDetails)) {
                // 7. Crear el objeto de autenticación
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,           // usuario autenticado
                                null,                  // credentials (no las necesitamos ya)
                                userDetails.getAuthorities()  // roles/authorities
                        );

                // 8. Guardar detalles extra de la request (IP, etc.)
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // 9. ¡Setear la autenticación en el contexto de seguridad!
                //    A partir de este momento, Spring Security considera al usuario autenticado.
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        // 10. Continuar la cadena de filtros (request → controlador → response)
        filterChain.doFilter(request, response);
    }
}

package com.columbia.viajes.security;

import com.columbia.viajes.security.service.JwtService;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.filter.OncePerRequestFilter;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter{

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        
        String encabezado = request.getHeader("Authorization");
        
        if(encabezado == null || !encabezado.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }
        
        String token = encabezado.substring(7);
        
        try {
            var datosToken = jwtService.leerToken(token);
            UsuarioAutenticado usuario = (UsuarioAutenticado)
                    userDetailsService.loadUserByUsername(datosToken.nombreUsuario());
            
            if(usuario.getVersionAutenticacion() != datosToken.versionAutenticacion()){
                throw new JwtException("El token ya no es válido");
            }
            
            var autenticacion = 
                UsernamePasswordAuthenticationToken.authenticated(
                    usuario,
                    null,
                    usuario.getAuthorities()
            );
            
            var contexto = SecurityContextHolder.createEmptyContext();
            contexto.setAuthentication(autenticacion);
            SecurityContextHolder.setContext(contexto);
            
        } catch (JwtException | UsernameNotFoundException | IllegalArgumentException e) {
            
            SecurityContextHolder.clearContext();
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        
        filterChain.doFilter(request, response);
        
    }
    
}

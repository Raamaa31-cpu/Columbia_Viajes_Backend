package com.columbia.viajes.service;

import com.columbia.viajes.dto.request.AuthRequest;
import com.columbia.viajes.dto.response.AuthResponse;
import com.columbia.viajes.security.UsuarioAutenticado;
import com.columbia.viajes.security.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    
    public AuthResponse autenticar(AuthRequest request){
        var credenciales = UsernamePasswordAuthenticationToken.unauthenticated(request.identificador(), request.contrasenia());
        
        UsuarioAutenticado usuarioAutenticado = (UsuarioAutenticado) authenticationManager
                                                                    .authenticate(credenciales)
                                                                    .getPrincipal();
        
        String rolNombre = usuarioAutenticado.getAuthorities()
                .iterator()
                .next()
                .getAuthority();
        
        String token = jwtService.generarToken(usuarioAutenticado.getUsername(), usuarioAutenticado.getVersionAutenticacion());
        
        return new AuthResponse(usuarioAutenticado.getUsername(), rolNombre, token);
    }
    
}

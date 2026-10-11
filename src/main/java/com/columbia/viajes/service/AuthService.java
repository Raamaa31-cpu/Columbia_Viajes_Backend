package com.columbia.viajes.service;

import com.columbia.viajes.dto.request.AuthRequest;
import com.columbia.viajes.dto.response.AuthResponse;
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
        var credenciales = UsernamePasswordAuthenticationToken.unauthenticated(request.nombre(), request.contrasenia());
        
        var autenticacion = authenticationManager.authenticate(credenciales);
        
        String rolNombre = autenticacion.getAuthorities()
                .iterator()
                .next()
                .getAuthority();
        
        String token = jwtService.generarToken(autenticacion.getName());
        
        return new AuthResponse(autenticacion.getName(), rolNombre, token);
    }
    
}

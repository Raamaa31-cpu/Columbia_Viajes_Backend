package com.columbia.viajes.security.service;

import com.columbia.viajes.model.Usuario;
import com.columbia.viajes.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class UsuarioActualService {

    private final UsuarioRepository usuarioRepository;
    
    public Usuario obtener(){
        var autenticacion = SecurityContextHolder.getContext()
                .getAuthentication();
        
        if(autenticacion == null || !autenticacion.isAuthenticated() || autenticacion instanceof AnonymousAuthenticationToken){
            throw new InsufficientAuthenticationException(
                    "Necesitás iniciar sesión."
            );
        }
        
        Usuario usuarioActual = usuarioRepository.findByNombre(autenticacion.getName())
                .orElseThrow(() ->
                        new InsufficientAuthenticationException("Necesitás iniciar sesión.")
                );
        
        return usuarioActual;
    }
    
}

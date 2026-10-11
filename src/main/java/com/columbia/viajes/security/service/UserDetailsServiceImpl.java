package com.columbia.viajes.security.service;

import com.columbia.viajes.model.Usuario;
import com.columbia.viajes.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username){
        Usuario usuario = usuarioRepository.findByNombre(username)
                .orElseThrow(() -> 
                        new UsernameNotFoundException("Credenciales incorrectas")
                );
        
        return User.withUsername(usuario.getNombre())
                .password(usuario.getContrasenia())
                .authorities(usuario.getRol().getNombre())
                .build();
    }
    
}

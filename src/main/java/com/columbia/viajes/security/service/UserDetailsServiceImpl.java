package com.columbia.viajes.security.service;

import com.columbia.viajes.model.Usuario;
import com.columbia.viajes.model.Turista;
import com.columbia.viajes.repository.TuristaRepository;
import com.columbia.viajes.repository.UsuarioRepository;
import com.columbia.viajes.security.UsuarioAutenticado;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;
    private final TuristaRepository turistaRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username){
        Usuario usuario = usuarioRepository.findByNombre(username)
                .orElseThrow(() -> 
                        new UsernameNotFoundException("Credenciales incorrectas")
                );
        
        return new UsuarioAutenticado(usuario);
    }

    @Transactional(readOnly = true)
    public UserDetails loadUserByIdentificador(String identificador) {
        Optional<Usuario> porNombre = usuarioRepository.findByNombre(identificador);
        Optional<Usuario> porEmail = turistaRepository.findByEmailIgnoreCase(identificador)
                .map(Turista::getUsuario)
                .filter(usuario -> "Cliente".equals(usuario.getRol().getNombre()));

        if (porNombre.isPresent() && porEmail.isPresent()
                && !porNombre.get().getId().equals(porEmail.get().getId())) {
            throw new UsernameNotFoundException("Credenciales incorrectas");
        }

        Usuario usuario = porNombre.or(() -> porEmail)
                .orElseThrow(() -> new UsernameNotFoundException("Credenciales incorrectas"));

        return new UsuarioAutenticado(usuario);
    }
    
}

package com.columbia.viajes.security;

import com.columbia.viajes.model.Usuario;
import java.util.List;
import lombok.Getter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

@Getter
public class UsuarioAutenticado extends User {

    private final long versionAutenticacion;
    
    public UsuarioAutenticado(Usuario usuario){
        super(
            usuario.getNombre(),
            usuario.getContrasenia(),
            List.of(new SimpleGrantedAuthority(usuario.getRol().getNombre()))
        );
        
        this.versionAutenticacion = usuario.getVersionAutenticacion();
    }
    
}

package com.columbia.viajes.support;

import com.columbia.viajes.model.Rol;
import com.columbia.viajes.model.Usuario;
import com.columbia.viajes.security.DatosToken;
import com.columbia.viajes.security.UsuarioAutenticado;
import com.columbia.viajes.security.service.JwtService;
import org.springframework.security.core.userdetails.UserDetailsService;

import static org.mockito.Mockito.when;

public final class AutenticacionJwtPrueba {

    private AutenticacionJwtPrueba() {}

    public static String autenticar(JwtService jwt, UserDetailsService usuarios, String nombreRol) {
        Rol rol = new Rol();
        rol.setNombre(nombreRol);
        Usuario usuario = new Usuario();
        usuario.setNombre("cuenta-prueba");
        usuario.setContrasenia("hash-simulado");
        usuario.setRol(rol);
        String token = "token-de-prueba";
        when(jwt.leerToken(token)).thenReturn(new DatosToken(usuario.getNombre(), 0));
        when(usuarios.loadUserByUsername(usuario.getNombre())).thenReturn(new UsuarioAutenticado(usuario));
        return token;
    }
}

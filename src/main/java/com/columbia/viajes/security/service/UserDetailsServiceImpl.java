package com.columbia.viajes.security.service;

import com.columbia.viajes.model.Rol;
import com.columbia.viajes.model.Usuario;
import com.columbia.viajes.repository.UsuarioRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Implementación de {@link UserDetailsService} que carga usuarios desde la base de datos.
 *
 * <p>Spring Security llama a este servicio durante la autenticación para buscar
 * el usuario por su nombre de usuario. Si lo encuentra, construye un objeto
 * {@link UserDetails} con el username, password encriptado y authorities (roles).</p>
 *
 * <p>Los roles se mapean de la tabla {@code roles} a authorities de Spring Security
 * con el prefijo {@code ROLE_} (por convención). Por ejemplo,</p>
 * <ul>
 *   <li>{@code "Administrador"} → {@code "ROLE_ADMIN"}</li>
 *   <li>{@code "Cliente"} → {@code "ROLE_CLIENTE"}</li>
 *   <li>{@code "Vendedor"} → {@code "ROLE_VENDEDOR"}</li>
 *   <li>{@code "Dueño"} → {@code "ROLE_OWNER"}</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    /**
     * Busca un usuario en la base de datos por su nombre de usuario
     * y lo convierte en un {@link UserDetails} que Spring Security entiende.
     *
     * @param username el nombre de usuario (campo {@code nombre} de la tabla usuarios)
     * @throws UsernameNotFoundException si no existe el usuario
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByNombre(username)
                .orElseThrow(() ->
                        new UsernameNotFoundException("No se encontró el usuario con nombre: " + username));

        return User.builder()
                .username(usuario.getNombre())
                .password(usuario.getContrasenia())  // debe estar encriptada con BCrypt
                .authorities(mapRolesToAuthorities(usuario.getRol()))
                .build();
    }

    /**
     * Convierte el {@link Rol} del usuario en una lista de {@link GrantedAuthority}.
     *
     * <p>Spring Security usa authorities (strings) para decidir si un usuario
     * puede o no acceder a un endpoint o método. El prefijo {@code ROLE_} es
     * una convención que permite usar {@code hasRole("ADMIN")} en lugar de
     * {@code hasAuthority("ROLE_ADMIN")}.</p>
     */
    private List<GrantedAuthority> mapRolesToAuthorities(Rol rol) {
        String rolNombre = rol.getNombre();
        // Normalizamos "Dueño" → "OWNER" (sin caracteres especiales)
        String authority = switch (rolNombre) {
            case "Administrador" -> "ROLE_ADMIN";
            case "Vendedor" -> "ROLE_VENDEDOR";
            case "Dueño" -> "ROLE_OWNER";
            case "Cliente" -> "ROLE_CLIENTE";
            default -> "ROLE_" + rolNombre.toUpperCase();
        };
        return List.of(new SimpleGrantedAuthority(authority));
    }
}

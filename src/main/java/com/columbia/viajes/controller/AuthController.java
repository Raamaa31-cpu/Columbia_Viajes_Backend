package com.columbia.viajes.controller;

import com.columbia.viajes.dto.request.AuthRequest;
import com.columbia.viajes.dto.response.AuthResponse;
import com.columbia.viajes.model.Usuario;
import com.columbia.viajes.repository.UsuarioRepository;
import com.columbia.viajes.security.JwtUtil;
import com.columbia.viajes.security.service.UserDetailsServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador de autenticación.
 *
 * <p>Endpoint público (no requiere token) que recibe las credenciales del usuario,
 * las valida y devuelve un token JWT.</p>
 *
 * <p><b>Flujo:</b></p>
 * <ol>
 *   <li>El cliente envía POST /api/auth/login con { nombre, contrasenia }.</li>
 *   <li>{@code AuthenticationManager} compara la contraseña con BCrypt.</li>
 *   <li>Si es correcto, se genera un token JWT con {@link JwtUtil}.</li>
 *   <li>El token se devuelve al cliente para usarlo en requests subsecuentes.</li>
 * </ol>
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    // Orquesta la autenticación: compara username + password en cruzado (raw vs BCrypt)
    private final AuthenticationManager authenticationManager;
    // Servicio que carga el usuario desde la DB y genera authorities
    private final UserDetailsServiceImpl userDetailsServiceImpl;
    // Genera y valida el JWT
    private final JwtUtil jwtUtil;
    // Repositorio para buscar datos extra del usuario (id, nombre, rol) al generar la respuesta
    private final UsuarioRepository usuarioRepository;

    /**
     * Endpoint de login: POST /api/auth/login
     *
     * @param request contiene nombre y contraseña del usuario
     * @return ResponseEntity con AuthResponse (incluye el token JWT) o 401 si falla la autenticación
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
        try {
            // AuthenticationManager.authenticated() lanza AuthenticationException
            // si el usuario no existe o la contraseña no coincide
            AuthenticationManager auth = authenticationManager;
            auth.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.nombre(),
                            request.contrasenia()
                    )
            );
        } catch (AuthenticationException e) {
            return ResponseEntity.status(401).build();
        }

        // Cargar el usuario completo (con su rol) para generar el token y la respuesta
        Usuario usuario = usuarioRepository.findByNombre(request.nombre())
                .orElseThrow(() -> new RuntimeException("Error inesperado: usuario no encontrado"));

        UserDetails userDetails = userDetailsServiceImpl.loadUserByUsername(usuario.getNombre());

        // Generar el token JWT
        String token = jwtUtil.generarToken(userDetails);

        // Construir la respuesta con datos básicos del usuario + token
        AuthResponse response = new AuthResponse(
                token,
                usuario.getId(),
                usuario.getNombre(),
                usuario.getRol().getNombre()
        );

        return ResponseEntity.ok(response);
    }
}

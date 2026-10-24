package com.columbia.viajes.service;

import com.columbia.viajes.dto.request.AuthRequest;
import com.columbia.viajes.model.Rol;
import com.columbia.viajes.model.Turista;
import com.columbia.viajes.model.Usuario;
import com.columbia.viajes.repository.TuristaRepository;
import com.columbia.viajes.repository.UsuarioRepository;
import com.columbia.viajes.security.AuthenticationConfig;
import com.columbia.viajes.security.UsuarioAutenticado;
import com.columbia.viajes.security.service.JwtService;
import com.columbia.viajes.security.service.UserDetailsServiceImpl;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    private UsuarioRepository usuarios;
    private TuristaRepository turistas;
    private JwtService jwt;
    private UserDetailsServiceImpl detalles;
    private BCryptPasswordEncoder encoder;
    private AuthService service;

    @BeforeEach
    void preparar() {
        usuarios = mock(UsuarioRepository.class);
        turistas = mock(TuristaRepository.class);
        jwt = mock(JwtService.class);
        encoder = new BCryptPasswordEncoder(4);
        detalles = new UserDetailsServiceImpl(usuarios, turistas);
        AuthenticationConfig config = new AuthenticationConfig();
        var provider = config.authenticationProvider(detalles, encoder);
        service = new AuthService(config.authenticationManager(provider), jwt);
        when(jwt.generarToken(anyString(), anyLong())).thenReturn("jwt_de_prueba");
    }

    @ParameterizedTest
    @ValueSource(strings = {"juan_viajes", "JUAN.PEREZ@example.com"})
    void clienteIngresaConNombreOEmailYElJwtConservaElNombreCanonico(String identificador) {
        prepararCliente();

        var response = service.autenticar(new AuthRequest(identificador, "ClavePrueba123"));

        assertEquals("juan_viajes", response.nombre());
        assertEquals("Cliente", response.rolNombre());
        assertEquals("jwt_de_prueba", response.token());
        verify(jwt).generarToken("juan_viajes", 3);
    }

    @ParameterizedTest
    @ValueSource(strings = {"Administrador", "Dueño", "Vendedor"})
    void rolesDeGestionConservanLoginConNombre(String nombreRol) {
        Usuario usuario = usuario(10, "gestion_prueba", nombreRol);
        when(usuarios.findByNombre("gestion_prueba")).thenReturn(Optional.of(usuario));

        var response = service.autenticar(new AuthRequest("gestion_prueba", "ClavePrueba123"));

        assertEquals(nombreRol, response.rolNombre());
        verify(jwt).generarToken("gestion_prueba", 3);
    }

    @ParameterizedTest
    @ValueSource(strings = {"juan_viajes", "JUAN.PEREZ@example.com"})
    void contraseniaIncorrectaSeRechazaConAmbosIdentificadores(String identificador) {
        prepararCliente();

        assertThrows(BadCredentialsException.class,
                () -> service.autenticar(new AuthRequest(identificador, "ClaveIncorrecta123")));

        verifyNoInteractions(jwt);
    }

    @ParameterizedTest
    @ValueSource(strings = {"Administrador", "Dueño", "Vendedor"})
    void emailDeUnPerfilAsociadoAOtroRolNoPermiteLogin(String nombreRol) {
        Usuario usuario = usuario(10, "gestion_prueba", nombreRol);
        when(turistas.findByEmailIgnoreCase("gestion@example.com"))
                .thenReturn(Optional.of(turista(usuario)));

        assertThrows(BadCredentialsException.class,
                () -> service.autenticar(new AuthRequest("gestion@example.com", "ClavePrueba123")));

        verifyNoInteractions(jwt);
    }

    @Test
    void identificadorInexistenteDevuelveCredencialesIncorrectasSinToken() {
        assertThrows(BadCredentialsException.class,
                () -> service.autenticar(new AuthRequest("no_existe", "ClavePrueba123")));

        verifyNoInteractions(jwt);
    }

    @Test
    void nombreYEmailIgualesDeLaMismaCuentaPermitenLogin() {
        Usuario usuario = usuario(10, "juan@example.com", "Cliente");
        when(usuarios.findByNombre("juan@example.com")).thenReturn(Optional.of(usuario));
        when(turistas.findByEmailIgnoreCase("juan@example.com"))
                .thenReturn(Optional.of(turista(usuario)));

        assertEquals("Cliente", service.autenticar(new AuthRequest("juan@example.com", "ClavePrueba123")).rolNombre());
        verify(jwt).generarToken("juan@example.com", 3);
    }

    @ParameterizedTest
    @ValueSource(strings = {"Cliente", "Administrador", "Dueño", "Vendedor"})
    void nombreDeUnaCuentaQueCoincideConEmailDeOtraSeRechaza(String rolDelNombre) {
        Usuario porNombre = usuario(10, "colision@example.com", rolDelNombre);
        Usuario porEmail = usuario(20, "otro_cliente", "Cliente");
        when(usuarios.findByNombre("colision@example.com")).thenReturn(Optional.of(porNombre));
        when(turistas.findByEmailIgnoreCase("colision@example.com"))
                .thenReturn(Optional.of(turista(porEmail)));

        assertThrows(BadCredentialsException.class,
                () -> service.autenticar(new AuthRequest("colision@example.com", "ClavePrueba123")));

        verifyNoInteractions(jwt);
    }

    @Test
    void cargaCanonicaUsadaPorElJwtNoReinterpretaElNombreComoEmail() {
        Usuario porNombre = usuario(10, "colision@example.com", "Dueño");
        when(usuarios.findByNombre("colision@example.com")).thenReturn(Optional.of(porNombre));

        UsuarioAutenticado usuario = (UsuarioAutenticado) detalles.loadUserByUsername("colision@example.com");

        assertEquals("colision@example.com", usuario.getUsername());
        assertEquals("Dueño", usuario.getAuthorities().iterator().next().getAuthority());
        verifyNoInteractions(turistas);
    }

    private void prepararCliente() {
        Usuario usuario = usuario(10, "juan_viajes", "Cliente");
        when(usuarios.findByNombre("juan_viajes")).thenReturn(Optional.of(usuario));
        when(turistas.findByEmailIgnoreCase("JUAN.PEREZ@example.com"))
                .thenReturn(Optional.of(turista(usuario)));
    }

    private Usuario usuario(Integer id, String nombre, String nombreRol) {
        Rol rol = new Rol();
        rol.setNombre(nombreRol);
        Usuario usuario = new Usuario();
        usuario.setId(id);
        usuario.setNombre(nombre);
        usuario.setRol(rol);
        usuario.setContrasenia(encoder.encode("ClavePrueba123"));
        usuario.setVersionAutenticacion(3);
        return usuario;
    }

    private Turista turista(Usuario usuario) {
        Turista turista = new Turista();
        turista.setId(usuario.getId());
        turista.setUsuario(usuario);
        return turista;
    }
}

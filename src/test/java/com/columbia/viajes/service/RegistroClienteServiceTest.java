package com.columbia.viajes.service;

import com.columbia.viajes.dto.request.RegistroClienteRequest;
import com.columbia.viajes.dto.request.TuristaRequest;
import com.columbia.viajes.dto.request.UsuarioActualizacionRequest;
import com.columbia.viajes.dto.request.UsuarioRequest;
import com.columbia.viajes.mapper.UsuarioMapper;
import com.columbia.viajes.model.Rol;
import com.columbia.viajes.model.Turista;
import com.columbia.viajes.model.Usuario;
import com.columbia.viajes.repository.RolRepository;
import com.columbia.viajes.repository.SucursalRepository;
import com.columbia.viajes.repository.TuristaRepository;
import com.columbia.viajes.repository.UsuarioRepository;
import com.columbia.viajes.security.service.UsuarioActualService;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.access.AccessDeniedException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class RegistroClienteServiceTest {

    private UsuarioRepository usuarios;
    private RolRepository roles;
    private UsuarioActualService usuarioActual;
    private TuristaService turistas;
    private TuristaRepository perfiles;
    private BCryptPasswordEncoder encoder;
    private UsuarioService service;
    private Rol rolCliente;

    @BeforeEach
    void preparar() {
        usuarios = mock(UsuarioRepository.class);
        roles = mock(RolRepository.class);
        usuarioActual = mock(UsuarioActualService.class);
        turistas = mock(TuristaService.class);
        perfiles = mock(TuristaRepository.class);
        encoder = new BCryptPasswordEncoder(4);
        service = new UsuarioService(usuarios, roles, mock(SucursalRepository.class),
                Mappers.getMapper(UsuarioMapper.class), encoder, usuarioActual, turistas,
                new ValidadorIdentificadoresService(usuarios, perfiles));
        rolCliente = rol(77, "Cliente");
        when(roles.findByNombre("Cliente")).thenReturn(Optional.of(rolCliente));
        when(usuarioActual.obtener()).thenReturn(actor("Dueño"));
        when(usuarios.save(any(Usuario.class))).thenAnswer(invocacion -> {
            Usuario usuario = invocacion.getArgument(0);
            if (usuario.getId() == null) {
                usuario.setId(50);
            }
            return usuario;
        });
    }

    @Test
    void registroAsignaClienteDesdeLaBaseGuardaHashYUsaElIdGeneradoParaElTurista() {
        RegistroClienteRequest request = request();

        var response = service.registrarCliente(request);

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarios).save(captor.capture());
        Usuario guardado = captor.getValue();
        assertSame(rolCliente, guardado.getRol());
        assertNull(guardado.getSucursal());
        assertEquals(0, guardado.getVersionAutenticacion());
        assertNotEquals(request.contrasenia(), guardado.getContrasenia());
        assertTrue(encoder.matches(request.contrasenia(), guardado.getContrasenia()));
        assertEquals(50, response.id());
        assertEquals("Cliente", response.rolNombre());
        verify(turistas).crear(50, request.turista());
        verify(roles, never()).findById(any());
        verifyNoInteractions(usuarioActual);
    }

    @Test
    void registroRechazaNombreOcupadoSinCrearUsuarioNiTurista() {
        Usuario existente = new Usuario();
        existente.setId(10);
        when(usuarios.findByNombre("ana_viajes")).thenReturn(Optional.of(existente));

        assertThrows(IllegalArgumentException.class, () -> service.registrarCliente(request()));

        verify(usuarios, never()).save(any());
        verifyNoInteractions(turistas, usuarioActual);
    }

    @Test
    void registroRechazaLaFaltaDelRolClienteSinGuardar() {
        when(roles.findByNombre("Cliente")).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class, () -> service.registrarCliente(request()));

        verify(usuarios, never()).save(any());
        verifyNoInteractions(turistas, usuarioActual);
    }

    @Test
    void registroPropagaElErrorAlCrearElTurista() {
        RegistroClienteRequest request = request();
        IllegalArgumentException error = new IllegalArgumentException("El email ya está registrado.");
        doThrow(error).when(turistas).crear(50, request.turista());

        IllegalArgumentException resultado = assertThrows(IllegalArgumentException.class,
                () -> service.registrarCliente(request));

        assertSame(error, resultado);
        verify(turistas).crear(50, request.turista());
    }

    @Test
    void creacionDeGestionConservaElRolSolicitadoYGuardaHash() {
        Rol rolVendedor = rol(2, "Vendedor");
        when(roles.findById(2)).thenReturn(Optional.of(rolVendedor));

        var response = service.crear(new UsuarioRequest("vendedor_nuevo", "ClavePrueba123", 2, null, null));

        assertEquals("Vendedor", response.rolNombre());
        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarios).save(captor.capture());
        assertTrue(encoder.matches("ClavePrueba123", captor.getValue().getContrasenia()));
        verifyNoInteractions(turistas);
    }

    @Test
    void editarPermiteConservarElNombrePropioSinCambiarHashNiVersion() {
        Usuario existente = new Usuario();
        existente.setId(10);
        existente.setNombre("ana_viajes");
        existente.setRol(rolCliente);
        existente.setContrasenia(encoder.encode("ClavePrueba123"));
        existente.setVersionAutenticacion(3);
        String hashOriginal = existente.getContrasenia();
        when(usuarios.findById(10)).thenReturn(Optional.of(existente));
        when(usuarios.findByNombre("ana_viajes")).thenReturn(Optional.of(existente));
        when(roles.findById(77)).thenReturn(Optional.of(rolCliente));

        var response = service.actualizar(10, new UsuarioActualizacionRequest("ana_viajes", 77, null));

        assertTrue(response.isPresent());
        assertEquals(hashOriginal, existente.getContrasenia());
        assertEquals(3, existente.getVersionAutenticacion());
        verifyNoInteractions(turistas);
    }

    @ParameterizedTest
    @CsvSource({
        "Dueño,Cliente,true", "Dueño,Vendedor,true", "Dueño,Administrador,true", "Dueño,Dueño,true",
        "Administrador,Cliente,true", "Administrador,Vendedor,true",
        "Administrador,Administrador,false", "Administrador,Dueño,false",
        "Vendedor,Cliente,false", "Vendedor,Vendedor,false", "Vendedor,Administrador,false", "Vendedor,Dueño,false",
        "Cliente,Cliente,false", "Cliente,Vendedor,false", "Cliente,Administrador,false", "Cliente,Dueño,false"
    })
    void creacionDeGestionRespetaLaJerarquia(String creador, String destino, boolean permitido) {
        when(usuarioActual.obtener()).thenReturn(actor(creador));
        when(roles.findById(42)).thenReturn(Optional.of(rol(42, destino)));
        TuristaRequest perfil = "Cliente".equals(destino) ? request().turista() : null;
        UsuarioRequest datos = new UsuarioRequest("cuenta_nueva", "ClavePrueba123", 42, null, perfil);

        if (permitido) {
            assertEquals(destino, service.crear(datos).rolNombre());
            if (perfil != null) {
                verify(turistas).crear(50, perfil);
            } else {
                verifyNoInteractions(turistas);
            }
        } else {
            assertThrows(AccessDeniedException.class, () -> service.crear(datos));
            verify(usuarios, never()).save(any());
            verifyNoInteractions(turistas);
        }
    }

    @Test
    void creacionDeClienteSinPerfilNoGuardaLaCuenta() {
        when(roles.findById(77)).thenReturn(Optional.of(rolCliente));

        assertThrows(IllegalArgumentException.class,
                () -> service.crear(new UsuarioRequest("cliente_incompleto", "ClavePrueba123", 77, null, null)));

        verify(usuarios, never()).save(any());
        verifyNoInteractions(turistas);
    }

    @Test
    void creacionDeVendedorConPerfilDeTuristaSeRechaza() {
        when(roles.findById(2)).thenReturn(Optional.of(rol(2, "Vendedor")));

        assertThrows(IllegalArgumentException.class, () -> service.crear(
                new UsuarioRequest("vendedor_nuevo", "ClavePrueba123", 2, null, request().turista())));

        verify(usuarios, never()).save(any());
        verifyNoInteractions(turistas);
    }

    @ParameterizedTest
    @ValueSource(strings = {"Administrador", "Dueño"})
    void administradorNoPuedePromoverUnVendedorAUnRolPrivilegiado(String destino) {
        when(usuarioActual.obtener()).thenReturn(actor("Administrador"));
        when(usuarios.findById(10)).thenReturn(Optional.of(actor("Vendedor")));
        when(roles.findById(42)).thenReturn(Optional.of(rol(42, destino)));

        assertThrows(AccessDeniedException.class, () -> service.actualizar(10,
                new UsuarioActualizacionRequest("cuenta_existente", 42, null)));

        verify(usuarios, never()).save(any());
    }

    @ParameterizedTest
    @ValueSource(strings = {"Administrador", "Dueño"})
    void administradorNoPuedeEditarUnaCuentaPrivilegiadaAunqueSoliciteVendedor(String rolExistente) {
        when(usuarioActual.obtener()).thenReturn(actor("Administrador"));
        when(usuarios.findById(10)).thenReturn(Optional.of(actor(rolExistente)));
        when(roles.findById(2)).thenReturn(Optional.of(rol(2, "Vendedor")));

        assertThrows(AccessDeniedException.class, () -> service.actualizar(10,
                new UsuarioActualizacionRequest("cuenta_existente", 2, null)));

        verify(usuarios, never()).save(any());
    }

    @ParameterizedTest
    @ValueSource(strings = {"Administrador", "Dueño"})
    void administradorNoPuedeEliminarUnaCuentaPrivilegiada(String rolExistente) {
        when(usuarioActual.obtener()).thenReturn(actor("Administrador"));
        when(usuarios.findById(10)).thenReturn(Optional.of(actor(rolExistente)));

        assertThrows(AccessDeniedException.class, () -> service.eliminar(10));

        verify(usuarios, never()).delete(any());
    }

    @ParameterizedTest
    @CsvSource({"Vendedor,Cliente", "Cliente,Vendedor"})
    void editarNoConvierteEntreClienteYCuentaDeGestion(String origen, String destino) {
        when(usuarios.findById(10)).thenReturn(Optional.of(actor(origen)));
        when(roles.findById(42)).thenReturn(Optional.of(rol(42, destino)));

        assertThrows(IllegalArgumentException.class, () -> service.actualizar(10,
                new UsuarioActualizacionRequest("cuenta_existente", 42, null)));

        verify(usuarios, never()).save(any());
    }

    private Usuario actor(String nombreRol) {
        Usuario usuario = new Usuario();
        usuario.setId(100);
        usuario.setRol(rol(999, nombreRol));
        return usuario;
    }

    @Test
    void registroRechazaNombreQueYaEsEmailAjenoAntesDeGuardar() {
        Turista perfilExistente = new Turista();
        perfilExistente.setId(20);
        when(perfiles.findByEmailIgnoreCase("ocupado@example.com")).thenReturn(Optional.of(perfilExistente));
        RegistroClienteRequest datos = new RegistroClienteRequest("ocupado@example.com", "ClavePrueba123", request().turista());

        assertThrows(IllegalArgumentException.class, () -> service.registrarCliente(datos));

        verify(usuarios, never()).save(any());
        verifyNoInteractions(turistas, usuarioActual);
    }

    @Test
    void registroRechazaEmailQueYaEsNombreAjenoAntesDeGuardar() {
        Usuario existente = actor("Vendedor");
        when(usuarios.findByNombre("ana@example.com")).thenReturn(Optional.of(existente));

        assertThrows(IllegalArgumentException.class, () -> service.registrarCliente(request()));

        verify(usuarios, never()).save(any());
        verifyNoInteractions(turistas, usuarioActual);
    }

    @Test
    void editarNombreNoPermiteTomarEmailDeOtraCuenta() {
        Usuario existente = actor("Cliente");
        existente.setNombre("ana_viajes");
        Turista perfilAjeno = new Turista();
        perfilAjeno.setId(20);
        when(usuarios.findById(100)).thenReturn(Optional.of(existente));
        when(roles.findById(77)).thenReturn(Optional.of(rolCliente));
        when(perfiles.findByEmailIgnoreCase("ajeno@example.com")).thenReturn(Optional.of(perfilAjeno));

        assertThrows(IllegalArgumentException.class, () -> service.actualizar(100,
                new UsuarioActualizacionRequest("ajeno@example.com", 77, null)));

        assertEquals("ana_viajes", existente.getNombre());
        verify(usuarios, never()).save(any());
    }

    @Test
    void editarNombrePermiteUsarElEmailPropio() {
        Usuario existente = actor("Cliente");
        existente.setNombre("ana_viajes");
        Turista perfilPropio = new Turista();
        perfilPropio.setId(100);
        when(usuarios.findById(100)).thenReturn(Optional.of(existente));
        when(roles.findById(77)).thenReturn(Optional.of(rolCliente));
        when(perfiles.findByEmailIgnoreCase("ana@example.com")).thenReturn(Optional.of(perfilPropio));

        var response = service.actualizar(100, new UsuarioActualizacionRequest("ana@example.com", 77, null));

        assertEquals("ana@example.com", response.orElseThrow().nombre());
        verify(usuarios).save(existente);
    }

    @Test
    void crearDesdeGestionRechazaNombreQueYaEsEmailAjeno() {
        when(roles.findById(2)).thenReturn(Optional.of(rol(2, "Vendedor")));
        Turista perfilAjeno = new Turista();
        perfilAjeno.setId(20);
        when(perfiles.findByEmailIgnoreCase("ajeno@example.com")).thenReturn(Optional.of(perfilAjeno));

        assertThrows(IllegalArgumentException.class, () -> service.crear(
                new UsuarioRequest("ajeno@example.com", "ClavePrueba123", 2, null, null)));

        verify(usuarios, never()).save(any());
        verifyNoInteractions(turistas);
    }

    private Rol rol(Integer id, String nombre) {
        Rol rol = new Rol();
        rol.setId(id);
        rol.setNombre(nombre);
        return rol;
    }

    private RegistroClienteRequest request() {
        return new RegistroClienteRequest("ana_viajes", "ClavePrueba123",
                new TuristaRequest("Ana", "Pérez", "Av. Siempre Viva 123", "ana@example.com", "1123456789"));
    }
}

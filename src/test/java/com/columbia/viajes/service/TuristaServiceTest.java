package com.columbia.viajes.service;

import com.columbia.viajes.dto.request.TuristaRequest;
import com.columbia.viajes.mapper.TuristaMapper;
import com.columbia.viajes.model.Rol;
import com.columbia.viajes.model.Turista;
import com.columbia.viajes.model.Usuario;
import com.columbia.viajes.repository.TuristaRepository;
import com.columbia.viajes.repository.UsuarioRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mapstruct.factory.Mappers;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TuristaServiceTest {

    private TuristaRepository turistas;
    private UsuarioRepository usuarios;
    private TuristaService service;

    @BeforeEach
    void preparar() {
        turistas = mock(TuristaRepository.class);
        usuarios = mock(UsuarioRepository.class);
        service = new TuristaService(turistas, usuarios, Mappers.getMapper(TuristaMapper.class),
                new ValidadorIdentificadoresService(usuarios, turistas));
    }

    @Test
    void crearRechazaEmailDeOtroTuristaSinGuardar() {
        when(turistas.findByEmailIgnoreCase("ana@example.com"))
                .thenReturn(Optional.of(turista(10, "ana@example.com")));

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> service.crear(20, request("ana@example.com")));

        assertEquals("Ya existe un turista registrado con el email: ana@example.com", error.getMessage());
        verify(turistas, never()).save(any());
        verifyNoInteractions(usuarios);
    }

    @Test
    void actualizarPermiteConservarElEmailPropio() {
        Turista existente = turista(10, "ana@example.com");
        Usuario usuarioVinculado = existente.getUsuario();
        when(turistas.findById(10)).thenReturn(Optional.of(existente));
        when(turistas.findByEmailIgnoreCase("ANA@example.com")).thenReturn(Optional.of(existente));
        when(turistas.save(existente)).thenReturn(existente);

        var response = service.actualizar(10, request("ANA@example.com"));

        assertTrue(response.isPresent());
        assertEquals("ANA@example.com", response.orElseThrow().email());
        assertSame(usuarioVinculado, existente.getUsuario());
        assertEquals(10, existente.getId());
        verify(usuarios, never()).findById(any());
        verify(turistas).save(existente);
    }

    @Test
    void actualizarRechazaEmailAjenoSinModificarElTurista() {
        Turista existente = turista(10, "ana@example.com");
        when(turistas.findById(10)).thenReturn(Optional.of(existente));
        when(turistas.findByEmailIgnoreCase("otro@example.com"))
                .thenReturn(Optional.of(turista(20, "otro@example.com")));

        assertThrows(IllegalArgumentException.class,
                () -> service.actualizar(10, request("otro@example.com")));

        assertEquals("ana@example.com", existente.getEmail());
        verify(turistas, never()).save(any());
        verifyNoInteractions(usuarios);
    }

    @Test
    void crearPermiteUnEmailDisponibleYConservaLaRelacionConElUsuario() {
        Usuario usuario = usuario(10, "Cliente");
        when(turistas.findByEmailIgnoreCase("ana@example.com")).thenReturn(Optional.empty());
        when(usuarios.findById(10)).thenReturn(Optional.of(usuario));
        when(turistas.save(any(Turista.class))).thenAnswer(invocacion -> invocacion.getArgument(0));

        var response = service.crear(10, request("ana@example.com"));

        assertEquals("ana@example.com", response.email());
        assertEquals(10, response.usuarioId());
        verify(turistas).save(any(Turista.class));
    }

    @Test
    void crearNoReemplazaElTuristaExistenteDelUsuario() {
        when(usuarios.findById(10)).thenReturn(Optional.of(usuario(10, "Cliente")));
        when(turistas.existsById(10)).thenReturn(true);

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> service.crear(10, request("nuevo@example.com")));

        assertEquals("El usuario con ID 10 ya tiene un turista asociado.", error.getMessage());
        verify(turistas, never()).save(any());
    }

    @ParameterizedTest
    @ValueSource(strings = {"Administrador", "Dueño", "Vendedor"})
    void crearRechazaUsuariosQueNoSonClientes(String nombreRol) {
        when(usuarios.findById(10)).thenReturn(Optional.of(usuario(10, nombreRol)));

        assertThrows(IllegalArgumentException.class,
                () -> service.crear(10, request("ana@example.com")));

        verify(turistas, never()).save(any());
    }

    @Test
    void crearRechazaUnUsuarioInexistente() {
        when(usuarios.findById(10)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> service.crear(10, request("ana@example.com")));

        verify(turistas, never()).save(any());
    }

    private Turista turista(Integer id, String email) {
        Usuario usuario = usuario(id, "Cliente");
        Turista turista = new Turista();
        turista.setId(id);
        turista.setUsuario(usuario);
        turista.setEmail(email);
        return turista;
    }

    @Test
    void crearRechazaEmailQueYaEsNombreDeOtraCuenta() {
        when(usuarios.findByNombre("ajeno@example.com")).thenReturn(Optional.of(usuario(20, "Vendedor")));

        assertThrows(IllegalArgumentException.class, () -> service.crear(10, request("ajeno@example.com")));

        verify(turistas, never()).save(any());
    }

    @Test
    void actualizarEmailNoPermiteTomarNombreDeOtraCuenta() {
        Turista existente = turista(10, "ana@example.com");
        when(turistas.findById(10)).thenReturn(Optional.of(existente));
        when(usuarios.findByNombre("ajeno@example.com")).thenReturn(Optional.of(usuario(20, "Vendedor")));

        assertThrows(IllegalArgumentException.class, () -> service.actualizar(10, request("ajeno@example.com")));

        assertEquals("ana@example.com", existente.getEmail());
        verify(turistas, never()).save(any());
    }

    @Test
    void actualizarEmailPermiteUsarElNombreDeLaPropiaCuenta() {
        Turista existente = turista(10, "anterior@example.com");
        Usuario usuarioOriginal = existente.getUsuario();
        when(turistas.findById(10)).thenReturn(Optional.of(existente));
        when(usuarios.findByNombre("ana@example.com")).thenReturn(Optional.of(existente.getUsuario()));
        when(turistas.save(existente)).thenReturn(existente);

        var response = service.actualizar(10, request("ana@example.com"));

        assertEquals("ana@example.com", response.orElseThrow().email());
        assertSame(usuarioOriginal, existente.getUsuario());
        verify(usuarios, never()).findById(any());
    }

    @Test
    void crearPermiteEmailIgualAlNombreDelUsuarioRelacionado() {
        Usuario usuario = usuario(10, "Cliente");
        usuario.setNombre("ana@example.com");
        when(usuarios.findByNombre("ana@example.com")).thenReturn(Optional.of(usuario));
        when(usuarios.findById(10)).thenReturn(Optional.of(usuario));
        when(turistas.save(any(Turista.class))).thenAnswer(invocacion -> invocacion.getArgument(0));

        var response = service.crear(10, request("ana@example.com"));

        assertEquals("ana@example.com", response.email());
        assertEquals(10, response.usuarioId());
    }

    private Usuario usuario(Integer id, String nombreRol) {
        Rol rol = new Rol();
        rol.setNombre(nombreRol);
        Usuario usuario = new Usuario();
        usuario.setId(id);
        usuario.setRol(rol);
        return usuario;
    }

    private TuristaRequest request(String email) {
        return new TuristaRequest("Ana", "Pérez", "Av. Siempre Viva 123", email, "1123456789");
    }
}

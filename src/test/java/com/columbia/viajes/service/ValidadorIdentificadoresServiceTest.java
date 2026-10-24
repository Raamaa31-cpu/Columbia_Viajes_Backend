package com.columbia.viajes.service;

import com.columbia.viajes.model.Turista;
import com.columbia.viajes.model.Usuario;
import com.columbia.viajes.repository.TuristaRepository;
import com.columbia.viajes.repository.UsuarioRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ValidadorIdentificadoresServiceTest {

    private UsuarioRepository usuarios;
    private TuristaRepository turistas;
    private ValidadorIdentificadoresService service;

    @BeforeEach
    void preparar() {
        usuarios = mock(UsuarioRepository.class);
        turistas = mock(TuristaRepository.class);
        service = new ValidadorIdentificadoresService(usuarios, turistas);
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(ints = {20})
    void nombreOcupadoPorOtroUsuarioSeRechaza(Integer idActual) {
        when(usuarios.findByNombre("ana")).thenReturn(Optional.of(usuario(10)));

        assertThrows(IllegalArgumentException.class, () -> service.validarNombreDisponible("ana", idActual));
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(ints = {20})
    void nombreQueCoincideConEmailAjenoSeRechaza(Integer idActual) {
        when(turistas.findByEmailIgnoreCase("ana@example.com")).thenReturn(Optional.of(turista(10)));

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> service.validarNombreDisponible("ana@example.com", idActual));

        assertEquals("El nombre de usuario coincide con un email registrado por otra cuenta.", error.getMessage());
    }

    @Test
    void nombrePuedeCoincidirConEmailYNombreDeLaPropiaCuenta() {
        when(usuarios.findByNombre("ana@example.com")).thenReturn(Optional.of(usuario(10)));
        when(turistas.findByEmailIgnoreCase("ana@example.com")).thenReturn(Optional.of(turista(10)));

        assertDoesNotThrow(() -> service.validarNombreDisponible("ana@example.com", 10));
    }

    @Test
    void nombreDisponibleSeAcepta() {
        assertDoesNotThrow(() -> service.validarNombreDisponible("ana", null));
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(ints = {20})
    void emailOcupadoPorOtroTuristaSeRechaza(Integer idActual) {
        when(turistas.findByEmailIgnoreCase("ana@example.com")).thenReturn(Optional.of(turista(10)));

        assertThrows(IllegalArgumentException.class, () -> service.validarEmailDisponible("ana@example.com", idActual));
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(ints = {20})
    void emailQueCoincideConNombreAjenoSeRechaza(Integer idActual) {
        when(usuarios.findByNombre("ana@example.com")).thenReturn(Optional.of(usuario(10)));

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> service.validarEmailDisponible("ana@example.com", idActual));

        assertEquals("El email coincide con un nombre de usuario registrado por otra cuenta.", error.getMessage());
    }

    @Test
    void emailPuedeCoincidirConNombreYEmailDeLaPropiaCuenta() {
        when(usuarios.findByNombre("ana@example.com")).thenReturn(Optional.of(usuario(10)));
        when(turistas.findByEmailIgnoreCase("ana@example.com")).thenReturn(Optional.of(turista(10)));

        assertDoesNotThrow(() -> service.validarEmailDisponible("ana@example.com", 10));
    }

    @Test
    void emailDisponibleSeAcepta() {
        assertDoesNotThrow(() -> service.validarEmailDisponible("ana@example.com", null));
    }

    private Usuario usuario(Integer id) {
        Usuario usuario = new Usuario();
        usuario.setId(id);
        return usuario;
    }

    private Turista turista(Integer id) {
        Turista turista = new Turista();
        turista.setId(id);
        turista.setUsuario(usuario(id));
        return turista;
    }
}

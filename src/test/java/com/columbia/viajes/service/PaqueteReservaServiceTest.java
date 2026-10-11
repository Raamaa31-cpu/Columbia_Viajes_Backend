package com.columbia.viajes.service;

import com.columbia.viajes.dto.request.PaqueteReservaRequest;
import com.columbia.viajes.mapper.PaqueteReservaMapper;
import com.columbia.viajes.model.*;
import com.columbia.viajes.repository.*;
import com.columbia.viajes.security.service.UsuarioActualService;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.CsvSource;
import org.mapstruct.factory.Mappers;
import org.springframework.security.access.AccessDeniedException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PaqueteReservaServiceTest {

    private PaqueteReservaRepository reservas;
    private PaqueteRepository paquetes;
    private TuristaRepository turistas;
    private UsuarioRepository usuarios;
    private UsuarioActualService usuarioActual;
    private PaqueteReservaService service;

    @BeforeEach
    void preparar() {
        reservas = mock(PaqueteReservaRepository.class);
        paquetes = mock(PaqueteRepository.class);
        turistas = mock(TuristaRepository.class);
        usuarios = mock(UsuarioRepository.class);
        usuarioActual = mock(UsuarioActualService.class);
        service = new PaqueteReservaService(reservas, paquetes, turistas, usuarios,
                Mappers.getMapper(PaqueteReservaMapper.class), usuarioActual);
    }

    @Test
    void vendedorNoPuedeEditarReservaFueraDeSuSucursal() {
        when(usuarioActual.obtener()).thenReturn(usuario(1, "Vendedor", 10));
        when(reservas.findByIdAndPaquete_Sucursal_Id(50, 10)).thenReturn(Optional.empty());

        assertTrue(service.actualizar(50, request(null)).isEmpty());

        verify(reservas, never()).findById(any());
        verify(reservas, never()).save(any());
        verifyNoInteractions(paquetes, turistas, usuarios);
    }

    @Test
    void editarReservaDeUnCompaneroConservaElVendedorAunqueElJsonIndiqueOtro() {
        Usuario compañero = usuario(2, "Vendedor", 10);
        PaqueteReserva reserva = prepararActualizacion(compañero, paquete(10));

        var response = service.actualizar(50, request(999));

        assertTrue(response.isPresent());
        assertSame(compañero, reserva.getVendedor());
        assertEquals(2, response.orElseThrow().vendedorId());
        verifyNoInteractions(usuarios);
        verify(reservas).save(reserva);
    }

    @Test
    void vendedorNoPuedeCambiarLaReservaAUnPaqueteDeOtraSucursal() {
        PaqueteReserva reserva = prepararActualizacion(usuario(2, "Vendedor", 10), paquete(20));
        Paquete paqueteOriginal = reserva.getPaquete();

        assertThrows(AccessDeniedException.class, () -> service.actualizar(50, request(null)));

        assertSame(paqueteOriginal, reserva.getPaquete());
        verify(reservas, never()).save(any());
    }

    @Test
    void crearComoVendedorIgnoraElVendedorDelJson() {
        Usuario vendedor = usuario(1, "Vendedor", 10);
        when(usuarioActual.obtener()).thenReturn(vendedor);
        prepararDatos(paquete(10));
        when(reservas.save(any())).thenAnswer(invocacion -> invocacion.getArgument(0));

        assertEquals(1, service.crear(request(999)).vendedorId());
        verifyNoInteractions(usuarios);
    }

    @ParameterizedTest
    @ValueSource(strings = {"Administrador", "Dueño"})
    void administradorYDuenoRecibenErrorDeDatosSiElVendedorEsDeOtraSucursal(String rol) {
        when(usuarioActual.obtener()).thenReturn(usuario(1, rol, null));
        prepararDatos(paquete(10));
        when(usuarios.findById(2)).thenReturn(Optional.of(usuario(2, "Vendedor", 20)));

        assertThrows(IllegalArgumentException.class, () -> service.crear(request(2)));
        verify(reservas, never()).save(any());
    }

    @ParameterizedTest
    @CsvSource({
        "TURISTA, MEDIA_PENSION, 1000.00",
        "TURISTA, PENSION_COMPLETA, 1100.00",
        "PRIMERA, MEDIA_PENSION, 1100.00",
        "PRIMERA, PENSION_COMPLETA, 1200.00"
    })
    void crearCalculaLosAdicionalesSobreElPrecioBase(ClaseVuelo clase, RegimenHospedaje regimen,
            BigDecimal precioEsperado) {
        when(usuarioActual.obtener()).thenReturn(usuario(1, "Vendedor", 10));
        prepararDatos(paquete(10));
        when(reservas.save(any())).thenAnswer(invocacion -> invocacion.getArgument(0));

        var response = service.crear(new PaqueteReservaRequest(100, 200, clase, regimen, null));

        assertEquals(precioEsperado, response.precioReserva());
    }

    @Test
    void actualizarRecalculaSinAcumularAdicionalesDeLaReservaAnterior() {
        PaqueteReserva reserva = prepararActualizacion(usuario(2, "Vendedor", 10), paquete(10));
        reserva.setPrecioReserva(new BigDecimal("1200.00"));

        var response = service.actualizar(50, request(null)).orElseThrow();

        assertEquals(new BigDecimal("1000.00"), response.precioReserva());
    }

    @Test
    void calcularRedondeaElResultadoFinalADosDecimales() {
        when(usuarioActual.obtener()).thenReturn(usuario(1, "Vendedor", 10));
        Paquete paquete = paquete(10);
        paquete.setPrecio(new BigDecimal("10.05"));
        prepararDatos(paquete);
        when(reservas.save(any())).thenAnswer(invocacion -> invocacion.getArgument(0));

        var response = service.crear(new PaqueteReservaRequest(100, 200,
                ClaseVuelo.PRIMERA, RegimenHospedaje.MEDIA_PENSION, null));

        assertEquals(new BigDecimal("11.06"), response.precioReserva());
    }

    private PaqueteReserva prepararActualizacion(Usuario vendedorOriginal, Paquete nuevoPaquete) {
        when(usuarioActual.obtener()).thenReturn(usuario(1, "Vendedor", 10));
        PaqueteReserva reserva = new PaqueteReserva();
        reserva.setId(50);
        reserva.setPaquete(paquete(10));
        reserva.setVendedor(vendedorOriginal);
        when(reservas.findByIdAndPaquete_Sucursal_Id(50, 10)).thenReturn(Optional.of(reserva));
        prepararDatos(nuevoPaquete);
        when(reservas.save(any())).thenAnswer(invocacion -> invocacion.getArgument(0));
        return reserva;
    }

    private void prepararDatos(Paquete paquete) {
        when(paquetes.findById(100)).thenReturn(Optional.of(paquete));
        Turista turista = new Turista();
        turista.setId(200);
        when(turistas.findById(200)).thenReturn(Optional.of(turista));
    }

    private PaqueteReservaRequest request(Integer idVendedor) {
        return new PaqueteReservaRequest(100, 200, ClaseVuelo.values()[0],
                RegimenHospedaje.values()[0], idVendedor);
    }

    private Paquete paquete(Integer idSucursal) {
        Paquete paquete = new Paquete();
        paquete.setId(100);
        paquete.setPrecio(new BigDecimal("1000.00"));
        paquete.setSucursal(sucursal(idSucursal));
        return paquete;
    }

    private Usuario usuario(Integer id, String nombreRol, Integer idSucursal) {
        Rol rol = new Rol();
        rol.setNombre(nombreRol);
        Usuario usuario = new Usuario();
        usuario.setId(id);
        usuario.setRol(rol);
        usuario.setSucursal(idSucursal == null ? null : sucursal(idSucursal));
        return usuario;
    }

    private Sucursal sucursal(Integer id) {
        Sucursal sucursal = new Sucursal();
        sucursal.setId(id);
        return sucursal;
    }
}

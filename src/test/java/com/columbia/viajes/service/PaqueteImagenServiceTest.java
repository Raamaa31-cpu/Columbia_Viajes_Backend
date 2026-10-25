package com.columbia.viajes.service;

import com.columbia.viajes.dto.request.PaqueteRequest;
import com.columbia.viajes.mapper.PaqueteMapper;
import com.columbia.viajes.model.*;
import com.columbia.viajes.repository.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Optional;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mapstruct.factory.Mappers;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PaqueteImagenServiceTest {

    @TempDir
    Path directorio;

    private PaqueteRepository paquetes;
    private ImagenService imagenes;
    private PaqueteService service;
    private Paquete paquete;
    private TransactionTemplate transaccion;
    private SucursalRepository sucursales;
    private VueloRepository vuelos;
    private HotelRepository hoteles;

    @BeforeEach
    void preparar() {
        paquetes = mock(PaqueteRepository.class);
        imagenes = spy(new ImagenService(directorio.toString()));
        sucursales = mock(SucursalRepository.class);
        vuelos = mock(VueloRepository.class);
        hoteles = mock(HotelRepository.class);
        service = new PaqueteService(paquetes, sucursales, vuelos, hoteles,
                Mappers.getMapper(PaqueteMapper.class), imagenes);
        paquete = new Paquete();
        paquete.setId(1);
        when(paquetes.findByIdParaActualizar(1)).thenReturn(Optional.of(paquete));
        when(paquetes.save(any())).thenAnswer(invocacion -> {
            Paquete guardado = invocacion.getArgument(0);
            if (guardado.getId() == null) {
                guardado.setId(20);
            }
            return guardado;
        });
        // Activa los callbacks transaccionales de Spring sin conectar a una base de datos.
        transaccion = new TransactionTemplate(new TransaccionesDePrueba());
    }

    @Test
    void asociaLaPrimeraImagenYDevuelveSuNombre() throws IOException {
        var archivo = archivo();

        var respuesta = transaccion.execute(estado -> service.actualizarImagen(1, archivo));

        assertNotNull(respuesta.imagen());
        assertEquals("/api/paquetes/1/imagen", respuesta.imagen());
        assertTrue(Files.exists(directorio.resolve(paquete.getImagen())));
        verify(paquetes).findByIdParaActualizar(1);
        verify(paquetes).save(paquete);
        verify(imagenes, never()).eliminar(any());
    }

    @Test
    void conservaLaAnteriorHastaConfirmarYDespuesQuedaSoloLaNueva() throws IOException {
        String anterior = prepararImagenAnterior();
        var archivo = archivo();

        var respuesta = transaccion.execute(estado -> {
            var resultado = service.actualizarImagen(1, archivo);
            assertTrue(Files.exists(directorio.resolve(anterior)));
            assertTrue(Files.exists(directorio.resolve(paquete.getImagen())));
            return resultado;
        });

        assertFalse(Files.exists(directorio.resolve(anterior)));
        assertTrue(Files.exists(directorio.resolve(paquete.getImagen())));
        assertCantidadArchivos(1);
    }

    @Test
    void falloAlGuardarLaReferenciaLimpiaLaNuevaYConservaLaAnterior() throws IOException {
        String anterior = prepararImagenAnterior();
        var archivo = archivo();
        doThrow(new IllegalStateException("Fallo de persistencia simulado")).when(paquetes).save(any());

        assertThrows(IllegalStateException.class,
                () -> transaccion.execute(estado -> service.actualizarImagen(1, archivo)));

        assertTrue(Files.exists(directorio.resolve(anterior)));
        assertCantidadArchivos(1);
    }

    @Test
    void rollbackPosteriorALaLlamadaTambienLimpiaLaNueva() throws IOException {
        String anterior = prepararImagenAnterior();
        var archivo = archivo();

        var respuesta = transaccion.execute(estado -> {
            var resultado = service.actualizarImagen(1, archivo);
            estado.setRollbackOnly();
            return resultado;
        });

        assertTrue(Files.exists(directorio.resolve(anterior)));
        assertFalse(Files.exists(directorio.resolve(paquete.getImagen())));
        assertCantidadArchivos(1);
    }

    @Test
    void paqueteInexistenteNoGuardaUnArchivo() throws IOException {
        when(paquetes.findByIdParaActualizar(1)).thenReturn(Optional.empty());
        var archivo = archivo();

        var error = assertThrows(ResponseStatusException.class,
                () -> transaccion.execute(estado -> service.actualizarImagen(1, archivo)));

        assertEquals(404, error.getStatusCode().value());
        verify(imagenes, never()).guardar(any());
        assertCantidadArchivos(0);
    }

    @Test
    void imagenInvalidaNoModificaLaReferenciaNiBorraLaAnterior() throws IOException {
        String anterior = prepararImagenAnterior();
        var invalida = new MockMultipartFile("imagen", new byte[]{65, 66, 67});

        assertThrows(IllegalArgumentException.class,
                () -> transaccion.execute(estado -> service.actualizarImagen(1, invalida)));

        assertEquals(anterior, paquete.getImagen());
        assertTrue(Files.exists(directorio.resolve(anterior)));
        verify(paquetes, never()).save(any());
    }

    @Test
    void eliminarPaqueteConfirmadoTambienRetiraSuImagen() throws IOException {
        String anterior = prepararImagenAnterior();

        transaccion.executeWithoutResult(estado -> {
            service.eliminar(1);
            assertTrue(Files.exists(directorio.resolve(anterior)));
        });

        assertFalse(Files.exists(directorio.resolve(anterior)));
    }

    @Test
    void eliminarPaqueteRevertidoConservaSuImagen() throws IOException {
        String anterior = prepararImagenAnterior();

        transaccion.executeWithoutResult(estado -> {
            service.eliminar(1);
            estado.setRollbackOnly();
        });

        assertTrue(Files.exists(directorio.resolve(anterior)));
    }

    @Test
    void falloDeLimpiezaNoInformaComoFallidaUnaReferenciaYaConfirmada() throws IOException {
        String anterior = prepararImagenAnterior();
        var archivo = archivo();
        doThrow(new UncheckedIOException(new IOException("Fallo de limpieza simulado")))
                .when(imagenes).eliminar(anterior);

        var respuesta = transaccion.execute(estado -> service.actualizarImagen(1, archivo));

        assertEquals("/api/paquetes/1/imagen", respuesta.imagen());
        assertTrue(Files.exists(directorio.resolve(paquete.getImagen())));
        assertTrue(Files.exists(directorio.resolve(anterior)));
    }

    @Test
    void actualizarDatosConservaLaImagenYCrearNoPermiteAsignarlaPorMapper() {
        PaqueteMapper mapper = Mappers.getMapper(PaqueteMapper.class);
        paquete.setImagen("referencia-existente.png");
        var request = new PaqueteRequest("Nuevo nombre", "Descripcion", LocalDate.of(2026, 11, 15),
                LocalDate.of(2026, 11, 22), new BigDecimal("850000.00"), 2, 1, 2, 1);
        var sucursal = new Sucursal();
        var ida = new Vuelo();
        var vuelta = new Vuelo();
        var hotel = new Hotel();
        when(sucursales.findById(2)).thenReturn(Optional.of(sucursal));
        when(vuelos.findById(1)).thenReturn(Optional.of(ida));
        when(vuelos.findById(2)).thenReturn(Optional.of(vuelta));
        when(hoteles.findById(1)).thenReturn(Optional.of(hotel));

        var resultado = transaccion.execute(estado -> service.actualizar(1, request));

        assertEquals("/api/paquetes/1/imagen", resultado.orElseThrow().imagen());
        assertEquals("referencia-existente.png", paquete.getImagen());
        assertEquals("Nuevo nombre", paquete.getNombre());
        assertNull(mapper.toPaquete(request, sucursal, ida, vuelta, hotel).getImagen());
    }

    @Test
    void creaPaqueteConImagenEnUnSoloGuardado() throws IOException {
        var request = prepararCreacion();
        var archivo = archivo();

        var respuesta = transaccion.execute(estado -> service.crear(request, archivo));

        assertEquals("Bariloche", respuesta.nombre());
        assertNotNull(respuesta.imagen());
        assertEquals("/api/paquetes/20/imagen", respuesta.imagen());
        var captor = org.mockito.ArgumentCaptor.forClass(Paquete.class);
        verify(paquetes, times(1)).save(captor.capture());
        assertTrue(Files.exists(directorio.resolve(captor.getValue().getImagen())));
        verify(paquetes, never()).findByIdParaActualizar(any());
        assertCantidadArchivos(1);
    }

    @Test
    void crearSinArchivoConservaLaOpcionSinImagen() throws IOException {
        var request = prepararCreacion();

        var respuesta = transaccion.execute(estado -> service.crear(request, null));

        assertNull(respuesta.imagen());
        verify(imagenes, never()).guardar(any());
        verify(paquetes).save(any(Paquete.class));
        assertCantidadArchivos(0);
    }

    @Test
    void imagenVaciaEnCreacionSeRechazaAntesDeGuardarElPaquete() throws IOException {
        var request = prepararCreacion();
        var vacia = new MockMultipartFile("imagen", new byte[0]);

        assertThrows(IllegalArgumentException.class,
                () -> transaccion.execute(estado -> service.crear(request, vacia)));

        verify(paquetes, never()).save(any());
        assertCantidadArchivos(0);
    }

    @Test
    void falloAlCrearPaqueteLimpiaLaImagenNueva() throws IOException {
        var request = prepararCreacion();
        var archivo = archivo();
        doThrow(new IllegalStateException("Fallo de persistencia simulado")).when(paquetes).save(any());

        assertThrows(IllegalStateException.class,
                () -> transaccion.execute(estado -> service.crear(request, archivo)));

        assertCantidadArchivos(0);
    }

    @Test
    void rollbackPosteriorACrearTambienLimpiaLaImagen() throws IOException {
        var request = prepararCreacion();
        var archivo = archivo();

        transaccion.executeWithoutResult(estado -> {
            service.crear(request, archivo);
            estado.setRollbackOnly();
        });

        assertCantidadArchivos(0);
    }

    @Test
    void hotelInexistenteSeRechazaAntesDeGuardarImagen() throws IOException {
        var request = prepararCreacion();
        when(hoteles.findById(1)).thenReturn(Optional.empty());
        var archivo = archivo();

        assertThrows(IllegalArgumentException.class,
                () -> transaccion.execute(estado -> service.crear(request, archivo)));

        verify(imagenes, never()).guardar(any());
        verify(paquetes, never()).save(any());
        assertCantidadArchivos(0);
    }

    private PaqueteRequest prepararCreacion() {
        when(sucursales.findById(2)).thenReturn(Optional.of(new Sucursal()));
        when(vuelos.findById(1)).thenReturn(Optional.of(new Vuelo()));
        when(vuelos.findById(2)).thenReturn(Optional.of(new Vuelo()));
        when(hoteles.findById(1)).thenReturn(Optional.of(new Hotel()));
        return new PaqueteRequest("Bariloche", "Descripcion", LocalDate.of(2026, 11, 15),
                LocalDate.of(2026, 11, 22), new BigDecimal("850000.00"), 2, 1, 2, 1);
    }

    @Test
    void lecturaDelPaqueteDevuelveUrlSinModificarElNombrePersistido() {
        paquete.setImagen("c5442565-bb68-485f-962e-ba501924ddaf.png");
        when(paquetes.findById(1)).thenReturn(Optional.of(paquete));

        assertEquals("/api/paquetes/1/imagen", service.obtener(1).orElseThrow().imagen());
        assertEquals("c5442565-bb68-485f-962e-ba501924ddaf.png", paquete.getImagen());
    }

    @Test
    void paqueteSinImagenDevuelveNullEnSuRespuesta() {
        when(paquetes.findById(1)).thenReturn(Optional.of(paquete));

        assertNull(service.obtener(1).orElseThrow().imagen());
    }

    @Test
    void obtieneElArchivoAsociadoAlIdDelPaquete() throws IOException {
        String nombre = prepararImagenAnterior();
        when(paquetes.findById(1)).thenReturn(Optional.of(paquete));

        var recurso = service.obtenerImagen(1);

        assertEquals(nombre, recurso.getFilename());
        try (var contenido = recurso.getInputStream()) {
            assertNotNull(ImageIO.read(contenido));
        }
    }

    @Test
    void lecturaDeImagenSinPaqueteDevuelve404() {
        when(paquetes.findById(1)).thenReturn(Optional.empty());

        var error = assertThrows(ResponseStatusException.class, () -> service.obtenerImagen(1));

        assertEquals(404, error.getStatusCode().value());
        verify(imagenes, never()).obtener(any());
    }

    @Test
    void lecturaDePaqueteSinFotoDevuelve404() {
        when(paquetes.findById(1)).thenReturn(Optional.of(paquete));

        var error = assertThrows(ResponseStatusException.class, () -> service.obtenerImagen(1));

        assertEquals(404, error.getStatusCode().value());
    }

    private String prepararImagenAnterior() throws IOException {
        String nombre = imagenes.guardar(archivo());
        paquete.setImagen(nombre);
        clearInvocations(imagenes);
        return nombre;
    }

    private MockMultipartFile archivo() throws IOException {
        try (var salida = new ByteArrayOutputStream()) {
            assertTrue(ImageIO.write(new BufferedImage(2, 3, BufferedImage.TYPE_INT_RGB), "PNG", salida));
            return new MockMultipartFile("imagen", "foto.png", "image/png", salida.toByteArray());
        }
    }

    private void assertCantidadArchivos(long cantidad) throws IOException {
        try (var archivos = Files.list(directorio)) {
            assertEquals(cantidad, archivos.count());
        }
    }

    private static class TransaccionesDePrueba extends AbstractPlatformTransactionManager {
        @Override
        protected Object doGetTransaction() { return new Object(); }

        @Override
        protected void doBegin(Object transaction, TransactionDefinition definition) {}

        @Override
        protected void doCommit(DefaultTransactionStatus status) {}

        @Override
        protected void doRollback(DefaultTransactionStatus status) {}
    }
}

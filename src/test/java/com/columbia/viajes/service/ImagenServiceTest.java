package com.columbia.viajes.service;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.NullSource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.*;

class ImagenServiceTest {

    @TempDir
    Path directorioTemporal;

    private Path directorioImagenes;
    private ImagenService service;

    @BeforeEach
    void preparar() {
        directorioImagenes = directorioTemporal.resolve("paquetes");
        service = new ImagenService(directorioImagenes.toString());
    }

    @ParameterizedTest
    @ValueSource(strings = {"PNG", "JPEG"})
    void guardaPngLegibleConNombrePropioYCreaLaCarpeta(String formato) throws IOException {
        var archivo = new MockMultipartFile("imagen", "../../foto.jpg", "image/jpeg", imagen(formato, 2, 3));

        String nombre = service.guardar(archivo);

        assertTrue(nombre.matches("[0-9a-f-]{36}\\.png"));
        Path guardada = directorioImagenes.resolve(nombre);
        assertTrue(Files.isRegularFile(guardada));
        try (var entrada = ImageIO.createImageInputStream(guardada.toFile())) {
            var lector = ImageIO.getImageReaders(entrada).next();
            try {
                assertEquals("png", lector.getFormatName().toLowerCase(java.util.Locale.ROOT));
                lector.setInput(entrada);
                assertEquals(2, lector.getWidth(0));
                assertEquals(3, lector.getHeight(0));
                assertNotNull(lector.read(0));
            } finally {
                lector.dispose();
            }
        }
        try (var archivos = Files.list(directorioImagenes)) {
            assertEquals(1, archivos.count());
        }
    }

    @Test
    void dosCargasTienenNombresDistintosYNoSeSobrescriben() throws IOException {
        String primera = service.guardar(new MockMultipartFile("imagen", imagen("PNG", 2, 3)));
        String segunda = service.guardar(new MockMultipartFile("imagen", imagen("PNG", 4, 5)));

        assertNotEquals(primera, segunda);
        assertEquals(2, ImageIO.read(directorioImagenes.resolve(primera).toFile()).getWidth());
        assertEquals(4, ImageIO.read(directorioImagenes.resolve(segunda).toFile()).getWidth());
    }

    @Test
    void archivoInvalidoNoCreaLaCarpetaNiGuardaContenido() {
        var archivo = new MockMultipartFile("imagen", "foto.jpg", "image/jpeg", new byte[]{65, 66, 67});

        assertThrows(IllegalArgumentException.class, () -> service.guardar(archivo));

        assertFalse(Files.exists(directorioImagenes));
    }

    @Test
    void falloDeAlmacenamientoSeInformaComoErrorDeEscritura() throws IOException {
        Path archivoExistente = directorioTemporal.resolve("archivo.txt");
        Files.writeString(archivoExistente, "conservar");
        ImagenService sinDirectorio = new ImagenService(archivoExistente.toString());
        var archivo = new MockMultipartFile("imagen", imagen("PNG", 2, 3));

        assertThrows(UncheckedIOException.class, () -> sinDirectorio.guardar(archivo));

        assertEquals("conservar", Files.readString(archivoExistente));
    }

    @ParameterizedTest
    @ValueSource(strings = {"PNG", "JPEG"})
    void aceptaImagenPorSuContenidoAunqueElNombreYTipoDeclaradoSeanDistintos(String formato) throws IOException {
        var archivo = new MockMultipartFile("imagen", "archivo.txt", "text/plain", imagen(formato, 2, 3));

        BufferedImage resultado = service.leerImagen(archivo);

        assertEquals(2, resultado.getWidth());
        assertEquals(3, resultado.getHeight());
    }

    @Test
    void eliminaSoloElArchivoGeneradoYSuAusenciaNoEsError() throws IOException {
        String nombre = service.guardar(new MockMultipartFile("imagen", imagen("PNG", 2, 3)));

        service.eliminar(nombre);
        service.eliminar(nombre);
        service.eliminar(null);

        assertFalse(Files.exists(directorioImagenes.resolve(nombre)));
    }

    @ParameterizedTest
    @ValueSource(strings = {"../archivo.txt", "C:\\archivo.txt", "https://ejemplo.com/foto.png", "foto.png"})
    void rechazaReferenciasNoGeneradasSinBorrarArchivos(String nombre) throws IOException {
        Path protegido = directorioTemporal.resolve("archivo.txt");
        Files.writeString(protegido, "conservar");

        assertThrows(IllegalArgumentException.class, () -> service.eliminar(nombre));

        assertEquals("conservar", Files.readString(protegido));
    }

    @Test
    void rechazaArchivoAusente() {
        assertThrows(IllegalArgumentException.class, () -> service.leerImagen(null));
    }

    @Test
    void rechazaArchivoVacio() {
        var archivo = new MockMultipartFile("imagen", new byte[0]);

        assertThrows(IllegalArgumentException.class, () -> service.leerImagen(archivo));
    }

    @Test
    void rechazaTextoAunqueSePresenteComoJpeg() {
        var archivo = new MockMultipartFile("imagen", "foto.jpg", "image/jpeg", new byte[]{65, 66, 67});

        assertThrows(IllegalArgumentException.class, () -> service.leerImagen(archivo));
    }

    @Test
    void rechazaFormatosFueraDeJpegYPng() throws IOException {
        var archivo = new MockMultipartFile("imagen", imagen("GIF", 2, 3));

        assertThrows(IllegalArgumentException.class, () -> service.leerImagen(archivo));
    }

    @ParameterizedTest
    @CsvSource({"4097,1", "1,4097"})
    void rechazaDimensionesExcesivasAunqueElArchivoComprimidoSeaChico(int ancho, int alto) throws IOException {
        var archivo = new MockMultipartFile("imagen", imagen("PNG", ancho, alto));

        assertThrows(IllegalArgumentException.class, () -> service.leerImagen(archivo));
    }

    @Test
    void rechazaImagenTruncada() throws IOException {
        byte[] completa = imagen("PNG", 2, 3);
        byte[] truncada = java.util.Arrays.copyOf(completa, 40);
        var archivo = new MockMultipartFile("imagen", truncada);

        assertThrows(IllegalArgumentException.class, () -> service.leerImagen(archivo));
    }

    private byte[] imagen(String formato, int ancho, int alto) throws IOException {
        var imagen = new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_RGB);
        try (var salida = new ByteArrayOutputStream()) {
            assertTrue(ImageIO.write(imagen, formato, salida));
            return salida.toByteArray();
        }
    }

    @Test
    void devuelveUnRecursoLegibleParaElArchivoGuardado() throws IOException {
        String nombre = service.guardar(new MockMultipartFile("imagen", imagen("PNG", 2, 3)));

        var recurso = service.obtener(nombre);

        assertEquals(nombre, recurso.getFilename());
        try (var contenido = recurso.getInputStream()) {
            assertEquals(2, ImageIO.read(contenido).getWidth());
        }
    }

    @Test
    void archivoInexistenteDevuelve404() {
        var error = assertThrows(ResponseStatusException.class,
                () -> service.obtener("c5442565-bb68-485f-962e-ba501924ddaf.png"));

        assertEquals(404, error.getStatusCode().value());
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"../archivo.txt", "C:\\archivo.txt", "https://ejemplo.com/foto.png", "foto.png"})
    void referenciaAusenteOInvalidaNoPermiteLeerOtrosArchivos(String nombre) {
        var error = assertThrows(ResponseStatusException.class, () -> service.obtener(nombre));

        assertEquals(404, error.getStatusCode().value());
    }
}

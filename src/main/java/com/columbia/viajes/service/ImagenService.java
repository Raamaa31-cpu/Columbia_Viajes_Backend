package com.columbia.viajes.service;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.UUID;
import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStream;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ImagenService {

    private static final int MAXIMO_LADO = 4096;

    private final Path directorio;

    public ImagenService(
            @Value("${app.imagenes.directorio}") String directorio) {
        this.directorio = Path.of(directorio)
                .toAbsolutePath()
                .normalize();
    }

    public String guardar(MultipartFile archivo) {
        BufferedImage imagen = leerImagen(archivo);
        String nombre = UUID.randomUUID() + ".png";

        try {
            Files.createDirectories(directorio);
            Path temporal = Files.createTempFile(directorio, ".carga-", ".tmp");
            try {
                try (var salida = Files.newOutputStream(temporal)) {
                    if (!ImageIO.write(imagen, "PNG", salida)) {
                        throw new IOException("No se pudo convertir la imagen a PNG.");
                    }
                }
                Files.move(temporal, directorio.resolve(nombre));
            } finally {
                Files.deleteIfExists(temporal);
            }
            return nombre;
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo guardar la imagen.", e);
        }
    }

    public BufferedImage leerImagen(MultipartFile archivo) {
        validarArchivo(archivo);

        try (var contenido = archivo.getInputStream();
                ImageInputStream entrada = ImageIO.createImageInputStream(contenido)) {
            if (entrada == null) {
                throw new IllegalArgumentException("No se pudo leer la imagen.");
            }

            var lectores = ImageIO.getImageReaders(entrada);
            if (!lectores.hasNext()) {
                throw new IllegalArgumentException("El archivo no contiene una imagen compatible.");
            }

            var lector = lectores.next();
            try {
                String formato = lector.getFormatName();
                if (!formato.equalsIgnoreCase("JPEG") && !formato.equalsIgnoreCase("PNG")) {
                    throw new IllegalArgumentException("La imagen debe ser JPEG o PNG.");
                }

                lector.setInput(entrada, true, true);
                validarDimensiones(lector.getWidth(0), lector.getHeight(0));
                return lector.read(0);
            } finally {
                lector.dispose();
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("No se pudo leer la imagen.", e);
        }
    }

    public Resource obtener(String nombre) {
        try {
            Path archivo = resolverArchivo(nombre);
            if (!Files.isRegularFile(archivo, LinkOption.NOFOLLOW_LINKS) || !Files.isReadable(archivo)) {
                throw new IllegalArgumentException("No se encontró el archivo.");
            }
            return new FileSystemResource(archivo);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No se encontró la imagen.");
        }
    }

    private Path resolverArchivo(String nombre) {
        if (nombre == null || !nombre.matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}\\.png")) {
            throw new IllegalArgumentException("La referencia de la imagen no es válida.");
        }

        Path archivo = directorio.resolve(nombre).normalize();
        if (!archivo.getParent().equals(directorio)) {
            throw new IllegalArgumentException("La referencia de la imagen no es válida.");
        }
        return archivo;
    }

    public void eliminar(String nombre) {
        if (nombre == null) {
            return;
        }
        Path archivo = resolverArchivo(nombre);
        try {
            Files.deleteIfExists(archivo);
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo eliminar la imagen.", e);
        }
    }

    private void validarDimensiones(int ancho, int alto) {
        if (ancho <= 0 || alto <= 0 || ancho > MAXIMO_LADO || alto > MAXIMO_LADO) {
            throw new IllegalArgumentException(
                    "La imagen debe tener entre 1 y " + MAXIMO_LADO + " pixeles por lado."
            );
        }
    }
    
    private void validarArchivo(MultipartFile archivo) {
        if (archivo == null || archivo.isEmpty()) {
            throw new IllegalArgumentException(
                    "Tenés que seleccionar una imagen."
            );
        }
    }
}

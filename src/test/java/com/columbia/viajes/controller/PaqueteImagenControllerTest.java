package com.columbia.viajes.controller;

import com.columbia.viajes.dto.response.PaqueteResponse;
import com.columbia.viajes.dto.request.PaqueteRequest;
import com.columbia.viajes.model.Rol;
import com.columbia.viajes.model.Usuario;
import com.columbia.viajes.security.DatosToken;
import com.columbia.viajes.security.SecurityConfig;
import com.columbia.viajes.security.UsuarioAutenticado;
import com.columbia.viajes.security.service.JwtService;
import com.columbia.viajes.service.PaqueteService;
import jakarta.servlet.Filter;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.mock.web.MockServletContext;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PaqueteImagenControllerTest {

    private AnnotationConfigWebApplicationContext contexto;
    private MockMvc mvc;
    private PaqueteService paquetes;
    private JwtService jwt;
    private UserDetailsService usuarios;

    @BeforeAll
    void iniciar() {
        contexto = new AnnotationConfigWebApplicationContext();
        contexto.setServletContext(new MockServletContext());
        contexto.register(ConfiguracionPrueba.class);
        contexto.refresh();
        paquetes = contexto.getBean(PaqueteService.class);
        jwt = contexto.getBean(JwtService.class);
        usuarios = contexto.getBean(UserDetailsService.class);
        mvc = MockMvcBuilders.webAppContextSetup(contexto)
                .addFilters(contexto.getBean("springSecurityFilterChain", Filter.class))
                .build();
    }

    @BeforeEach
    void preparar() {
        reset(paquetes, jwt, usuarios);
    }

    @AfterAll
    void cerrar() {
        contexto.close();
    }

    @ParameterizedTest
    @ValueSource(strings = {"Administrador", "Dueño"})
    void gestionPuedeEnviarElArchivoYRecibeElPaqueteActualizado(String rol) throws Exception {
        String token = autenticar(rol);
        MockMultipartFile archivo = archivo();
        when(paquetes.actualizarImagen(eq(1), any()))
                .thenReturn(new PaqueteResponse(1, "Bariloche", null, null, null,
                        null, "/api/paquetes/1/imagen", 2, 1, 2, 1));

        mvc.perform(multipart(HttpMethod.PUT, "/api/paquetes/1/imagen")
                        .file(archivo).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.imagen").value("/api/paquetes/1/imagen"));

        verify(paquetes).actualizarImagen(eq(1), same(archivo));
    }

    @ParameterizedTest
    @ValueSource(strings = {"Cliente", "Vendedor"})
    void otrosRolesReciben403SinLlamarAlService(String rol) throws Exception {
        String token = autenticar(rol);

        mvc.perform(multipart(HttpMethod.PUT, "/api/paquetes/1/imagen")
                        .file(archivo()).header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());

        verifyNoInteractions(paquetes);
    }

    @Test
    void sinTokenRecibe401SinLlamarAlService() throws Exception {
        mvc.perform(multipart(HttpMethod.PUT, "/api/paquetes/1/imagen").file(archivo()))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(paquetes);
    }

    @Test
    void faltaLaParteImagenDevuelve400() throws Exception {
        String token = autenticar("Administrador");

        mvc.perform(multipart(HttpMethod.PUT, "/api/paquetes/1/imagen")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(paquetes);
    }

    @Test
    void jsonNoReemplazaElArchivoMultipart() throws Exception {
        String token = autenticar("Administrador");

        mvc.perform(put("/api/paquetes/1/imagen")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"imagen\":\"foto.png\"}"))
                .andExpect(status().isUnsupportedMediaType());

        verifyNoInteractions(paquetes);
    }

    @Test
    void paqueteInexistenteConserva404() throws Exception {
        String token = autenticar("Administrador");
        when(paquetes.actualizarImagen(eq(999), any()))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "No se encontró el paquete."));

        mvc.perform(multipart(HttpMethod.PUT, "/api/paquetes/999/imagen")
                        .file(archivo()).header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound())
                .andExpect(content().string("No se encontró el paquete."));
    }

    @Test
    void archivoInvalidoConserva400() throws Exception {
        String token = autenticar("Administrador");
        when(paquetes.actualizarImagen(eq(1), any()))
                .thenThrow(new IllegalArgumentException("El archivo no contiene una imagen compatible."));

        mvc.perform(multipart(HttpMethod.PUT, "/api/paquetes/1/imagen")
                        .file(archivo()).header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest());
    }

    @Test
    void excesoDeTamanoInformadoPorSpringDevuelve413() throws Exception {
        String token = autenticar("Administrador");
        when(paquetes.actualizarImagen(eq(1), any()))
                .thenThrow(new MaxUploadSizeExceededException(5 * 1024 * 1024));

        mvc.perform(multipart(HttpMethod.PUT, "/api/paquetes/1/imagen")
                        .file(archivo()).header("Authorization", "Bearer " + token))
                .andExpect(status().is(413))
                .andExpect(content().string("La carga supera el tamaño permitido."));
    }

    @ParameterizedTest
    @ValueSource(strings = {"Administrador", "Dueño"})
    void gestionPuedeCrearConDatosJsonYArchivoEnLaMismaSolicitud(String rol) throws Exception {
        String token = autenticar(rol);
        var imagen = archivo();
        when(paquetes.crear(any(PaqueteRequest.class), same(imagen))).thenReturn(respuestaCreacion("foto.png"));

        mvc.perform(multipart("/api/paquetes").file(datosPaquete()).file(imagen)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(20))
                .andExpect(jsonPath("$.imagen").value("/api/paquetes/20/imagen"));

        verify(paquetes).crear(argThat(request -> request.nombre().equals("Bariloche")
                && request.idSucursal().equals(2) && request.idHotel().equals(1)), same(imagen));
    }

    @Test
    void crearConJsonCrudoSeRechazaCon415() throws Exception {
        String token = autenticar("Administrador");

        mvc.perform(post("/api/paquetes").contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPaquete()).header("Authorization", "Bearer " + token))
                .andExpect(status().isUnsupportedMediaType());

        verifyNoInteractions(paquetes);
    }

    @Test
    void multipartSinImagenPermiteCrearSoloConDatos() throws Exception {
        String token = autenticar("Administrador");
        when(paquetes.crear(any(PaqueteRequest.class), isNull())).thenReturn(respuestaCreacion(null));

        mvc.perform(multipart("/api/paquetes").file(datosPaquete())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isCreated());

        verify(paquetes).crear(any(PaqueteRequest.class), isNull());
    }

    @Test
    void datosJsonInvalidosSeRechazanAntesDeLlamarAlService() throws Exception {
        String token = autenticar("Administrador");
        var invalido = new MockMultipartFile("paquete", "", "application/json",
                jsonPaquete().replace("Bariloche", "").getBytes(StandardCharsets.UTF_8));

        mvc.perform(multipart("/api/paquetes").file(invalido).file(archivo())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(paquetes);
    }

    @Test
    void faltaPartePaqueteSeRechazaCon400() throws Exception {
        String token = autenticar("Administrador");

        mvc.perform(multipart("/api/paquetes").file(archivo())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(paquetes);
    }

    @Test
    void parteDeDatosDebeIndicarApplicationJson() throws Exception {
        String token = autenticar("Administrador");
        var texto = new MockMultipartFile("paquete", "", "text/plain",
                jsonPaquete().getBytes(StandardCharsets.UTF_8));

        mvc.perform(multipart("/api/paquetes").file(texto).file(archivo())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isUnsupportedMediaType());

        verifyNoInteractions(paquetes);
    }

    @ParameterizedTest
    @ValueSource(strings = {"Cliente", "Vendedor"})
    void creacionMultipartMantieneRestriccionPorRol(String rol) throws Exception {
        String token = autenticar(rol);

        mvc.perform(multipart("/api/paquetes").file(datosPaquete()).file(archivo())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());

        verifyNoInteractions(paquetes);
    }

    @Test
    void creacionMultipartSinTokenDevuelve401() throws Exception {
        mvc.perform(multipart("/api/paquetes").file(datosPaquete()).file(archivo()))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(paquetes);
    }

    private MockMultipartFile datosPaquete() {
        return new MockMultipartFile("paquete", "", "application/json",
                jsonPaquete().getBytes(StandardCharsets.UTF_8));
    }

    private String jsonPaquete() {
        return """
            {"nombre":"Bariloche","descripcion":"Viaje de prueba",
             "fechaLlegadaHotel":"2026-11-15","fechaPartidaHotel":"2026-11-22",
             "precio":850000.00,"idSucursal":2,"idVueloIda":1,"idVueloVuelta":2,"idHotel":1}
            """;
    }

    private PaqueteResponse respuestaCreacion(String imagen) {
        String url = imagen == null ? null : "/api/paquetes/20/imagen";
        return new PaqueteResponse(20, "Bariloche", null, null, null, null, url, 2, 1, 2, 1);
    }

    @Test
    void imagenEsPublicaYSeDevuelveComoPngConValidacionDeCache() throws Exception {
        var imagen = recursoPrueba();
        when(paquetes.obtenerImagen(1)).thenReturn(imagen);

        mvc.perform(get("/api/paquetes/1/imagen"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_PNG))
                .andExpect(content().bytes(imagen.getByteArray()))
                .andExpect(header().string("Cache-Control", "no-cache"))
                .andExpect(header().string("ETag", "\"c5442565-bb68-485f-962e-ba501924ddaf.png\""));

        verify(paquetes).obtenerImagen(1);
    }

    @Test
    void siElArchivoNoCambioDevuelve304SinReenviarLaImagen() throws Exception {
        when(paquetes.obtenerImagen(1)).thenReturn(recursoPrueba());

        mvc.perform(get("/api/paquetes/1/imagen")
                        .header("If-None-Match", "\"c5442565-bb68-485f-962e-ba501924ddaf.png\""))
                .andExpect(status().isNotModified())
                .andExpect(content().string(""));
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 999})
    void imagenAusenteOPaqueteInexistenteDevuelve404SinToken(int id) throws Exception {
        when(paquetes.obtenerImagen(id))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "No se encontró la imagen."));

        mvc.perform(get("/api/paquetes/" + id + "/imagen"))
                .andExpect(status().isNotFound());
    }

    private ByteArrayResource recursoPrueba() {
        return new ByteArrayResource(new byte[]{1, 2, 3}) {
            @Override
            public String getFilename() {
                return "c5442565-bb68-485f-962e-ba501924ddaf.png";
            }
        };
    }

    private String autenticar(String nombreRol) {
        Rol rol = new Rol();
        rol.setNombre(nombreRol);
        Usuario usuario = new Usuario();
        usuario.setNombre("cuenta-prueba");
        usuario.setContrasenia("hash-simulado");
        usuario.setRol(rol);
        String token = "token-de-prueba";
        when(jwt.leerToken(token)).thenReturn(new DatosToken(usuario.getNombre(), 0));
        when(usuarios.loadUserByUsername(usuario.getNombre())).thenReturn(new UsuarioAutenticado(usuario));
        return token;
    }

    private MockMultipartFile archivo() {
        return new MockMultipartFile("imagen", "foto.png", "image/png", new byte[]{1, 2, 3});
    }

    @Configuration(proxyBeanMethods = false)
    @EnableWebMvc
    @EnableWebSecurity
    @Import(SecurityConfig.class)
    static class ConfiguracionPrueba {
        @Bean
        PaqueteService paquetes() { return mock(PaqueteService.class); }

        @Bean
        JwtService jwt() { return mock(JwtService.class); }

        @Bean
        UserDetailsService usuarios() { return mock(UserDetailsService.class); }

        @Bean
        PaqueteController controller(PaqueteService service) { return new PaqueteController(service); }

        @Bean
        ManejadorErrores errores() { return new ManejadorErrores(); }
    }
}

package com.columbia.viajes.controller;

import com.columbia.viajes.dto.request.RegistroClienteRequest;
import com.columbia.viajes.dto.response.UsuarioResponse;
import com.columbia.viajes.service.AuthService;
import com.columbia.viajes.service.UsuarioService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AuthRegistroControllerTest {

    private UsuarioService usuarios;
    private MockMvc mvc;
    private LocalValidatorFactoryBean validator;

    @BeforeEach
    void preparar() {
        usuarios = mock(UsuarioService.class);
        validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mvc = MockMvcBuilders.standaloneSetup(new AuthController(mock(AuthService.class), usuarios))
                .setControllerAdvice(new ManejadorErrores())
                .setValidator(validator)
                .build();
    }

    @AfterEach
    void cerrar() {
        validator.close();
    }

    @Test
    void registroValidoDevuelve201SinContrasenia() throws Exception {
        when(usuarios.registrarCliente(any(RegistroClienteRequest.class)))
                .thenReturn(new UsuarioResponse(50, "ana_viajes", "Cliente", null, null));

        mvc.perform(post("/api/auth/registro").contentType(MediaType.APPLICATION_JSON)
                        .content(datos("ana@example.com")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.rolNombre").value("Cliente"))
                .andExpect(jsonPath("$.contrasenia").doesNotExist());

        verify(usuarios).registrarCliente(any(RegistroClienteRequest.class));
    }

    @Test
    void emailAnidadoInvalidoDevuelve400AntesDeLlamarAlService() throws Exception {
        mvc.perform(post("/api/auth/registro").contentType(MediaType.APPLICATION_JSON)
                        .content(datos("hola")))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(usuarios);
    }

    @Test
    void perfilAusenteDevuelve400AntesDeLlamarAlService() throws Exception {
        mvc.perform(post("/api/auth/registro").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombreUsuario":"ana_viajes","contrasenia":"ClavePrueba123"}
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(usuarios);
    }

    private String datos(String email) {
        return """
                {
                  "nombreUsuario":"ana_viajes",
                  "contrasenia":"ClavePrueba123",
                  "turista": {
                    "nombre":"Ana", "apellidos":"Pérez", "direccion":"Av. Siempre Viva 123",
                    "email":"%s", "telefono":"1123456789"
                  }
                }
                """.formatted(email);
    }
}

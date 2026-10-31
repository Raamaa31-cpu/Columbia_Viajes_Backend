package com.columbia.viajes.controller;

import com.columbia.viajes.security.SecurityConfig;
import com.columbia.viajes.security.service.JwtService;
import com.columbia.viajes.service.PaqueteReservaService;
import com.columbia.viajes.support.AutenticacionJwtPrueba;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PaqueteReservaController.class)
@Import({SecurityConfig.class, CorsControllerTest.SeguridadDePrueba.class})
class CorsControllerTest {
    private static final String ORIGEN = "http://localhost:8081";
    private static final String RUTA = "/api/paquetes-reservas/50";

    @Autowired private MockMvc mvc;
    @MockitoBean private PaqueteReservaService reservas;
    @MockitoBean private JwtService jwt;
    @MockitoBean private UserDetailsService usuarios;

    @TestConfiguration(proxyBeanMethods = false)
    @EnableWebSecurity
    static class SeguridadDePrueba {}

    @ParameterizedTest
    @ValueSource(strings = {"GET", "POST", "PUT", "DELETE", "OPTIONS"})
    void consultaPreviaPermitidaNoExigeJwt(String metodo) throws Exception {
        mvc.perform(options(RUTA).header("Origin", ORIGEN)
                        .header("Access-Control-Request-Method", metodo)
                        .header("Access-Control-Request-Headers", "authorization,content-type"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", ORIGEN))
                .andExpect(header().string("Access-Control-Allow-Methods", containsString(metodo)))
                .andExpect(header().string("Access-Control-Allow-Headers", containsString("authorization")))
                .andExpect(header().string("Access-Control-Allow-Headers", containsString("content-type")))
                .andExpect(header().doesNotExist("Access-Control-Allow-Credentials"));
        verifyNoInteractions(jwt, usuarios, reservas);
    }

    @ParameterizedTest
    @ValueSource(strings = {"https://pepito.com", "http://localhost:8082", "https://localhost:8081"})
    void otroOrigenNoSuperaLaConsultaPrevia(String origen) throws Exception {
        mvc.perform(options(RUTA).header("Origin", origen)
                        .header("Access-Control-Request-Method", "DELETE"))
                .andExpect(status().isForbidden())
                .andExpect(header().doesNotExist("Access-Control-Allow-Origin"));
        verifyNoInteractions(jwt, usuarios, reservas);
    }

    @Test
    void metodoNoPermitidoSeRechaza() throws Exception {
        mvc.perform(options(RUTA).header("Origin", ORIGEN)
                        .header("Access-Control-Request-Method", "PATCH"))
                .andExpect(status().isForbidden());
        verifyNoInteractions(jwt, usuarios, reservas);
    }

    @Test
    void encabezadoNoPermitidoSeRechaza() throws Exception {
        mvc.perform(options(RUTA).header("Origin", ORIGEN)
                        .header("Access-Control-Request-Method", "DELETE")
                        .header("Access-Control-Request-Headers", "X-No-Permitido"))
                .andExpect(status().isForbidden());
        verifyNoInteractions(jwt, usuarios, reservas);
    }

    @Test
    void origenPermitidoSigueNecesitandoToken() throws Exception {
        mvc.perform(delete(RUTA).header("Origin", ORIGEN))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string("Access-Control-Allow-Origin", ORIGEN));
        verifyNoInteractions(reservas);
    }

    @Test
    void clienteSigueSinPoderEliminarAunqueElOrigenEstePermitido() throws Exception {
        String token = AutenticacionJwtPrueba.autenticar(jwt, usuarios, "Cliente");
        mvc.perform(delete(RUTA).header("Origin", ORIGEN)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(header().string("Access-Control-Allow-Origin", ORIGEN));
        verifyNoInteractions(reservas);
    }

    @Test
    void duenoPuedeEliminarDesdeElOrigenPermitido() throws Exception {
        String token = AutenticacionJwtPrueba.autenticar(jwt, usuarios, "Dueño");
        mvc.perform(delete(RUTA).header("Origin", ORIGEN)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent())
                .andExpect(header().string("Access-Control-Allow-Origin", ORIGEN));
        verify(reservas).eliminar(50);
    }

    @Test
    void solicitudRealDeOtroOrigenSeRechazaAntesDelServicio() throws Exception {
        mvc.perform(delete(RUTA).header("Origin", "https://pepito.com"))
                .andExpect(status().isForbidden())
                .andExpect(header().doesNotExist("Access-Control-Allow-Origin"));
        verifyNoInteractions(jwt, usuarios, reservas);
    }
}

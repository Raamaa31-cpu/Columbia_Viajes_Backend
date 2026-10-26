package com.columbia.viajes.controller;

import com.columbia.viajes.dto.response.RankingVendedorResponse;
import com.columbia.viajes.security.SecurityConfig;
import com.columbia.viajes.security.service.JwtService;
import com.columbia.viajes.service.PaqueteReservaService;
import com.columbia.viajes.support.AutenticacionJwtPrueba;
import io.jsonwebtoken.JwtException;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PaqueteReservaController.class)
@Import({SecurityConfig.class, RankingVendedoresControllerTest.SeguridadDePrueba.class})
class RankingVendedoresControllerTest {

    private static final String RUTA = "/api/paquetes-reservas/ranking-vendedores";

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private PaqueteReservaService reservas;

    @MockitoBean
    private JwtService jwt;

    @MockitoBean
    private UserDetailsService usuarios;

    @TestConfiguration(proxyBeanMethods = false)
    @EnableWebSecurity
    static class SeguridadDePrueba {}

    @Test
    void duenoObtieneElRankingConLosImportesYElOrdenDelService() throws Exception {
        String token = AutenticacionJwtPrueba.autenticar(jwt, usuarios, "Dueño");
        when(reservas.rankingVendedores()).thenReturn(List.of(
                new RankingVendedorResponse(4, "vendedor_a1", new BigDecimal("1020000.00"), 2),
                new RankingVendedorResponse(6, "vendedor_b1", new BigDecimal("500000.00"), 3)
        ));

        mvc.perform(get(RUTA).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].vendedorId").value(4))
                .andExpect(jsonPath("$[0].nombre").value("vendedor_a1"))
                .andExpect(jsonPath("$[0].totalVendido").value(1020000.0))
                .andExpect(jsonPath("$[0].sucursalId").value(2))
                .andExpect(jsonPath("$[1].vendedorId").value(6))
                .andExpect(jsonPath("$[0].contrasenia").doesNotExist());

        verify(reservas).rankingVendedores();
        verifyNoMoreInteractions(reservas);
    }

    @Test
    void sinVentasDevuelve200ConListaVacia() throws Exception {
        String token = AutenticacionJwtPrueba.autenticar(jwt, usuarios, "Dueño");
        when(reservas.rankingVendedores()).thenReturn(List.of());

        mvc.perform(get(RUTA).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"Administrador", "Vendedor", "Cliente"})
    void otrosRolesReciben403SinLlamarAlService(String rol) throws Exception {
        String token = AutenticacionJwtPrueba.autenticar(jwt, usuarios, rol);

        mvc.perform(get(RUTA).header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());

        verifyNoInteractions(reservas);
    }

    @Test
    void sinTokenDevuelve401() throws Exception {
        mvc.perform(get(RUTA)).andExpect(status().isUnauthorized());

        verifyNoInteractions(reservas);
    }

    @Test
    void tokenInvalidoDevuelve401SinLlamarAlService() throws Exception {
        when(jwt.leerToken("invalido")).thenThrow(new JwtException("Token de prueba inválido"));

        mvc.perform(get(RUTA).header("Authorization", "Bearer invalido"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(reservas);
    }
}

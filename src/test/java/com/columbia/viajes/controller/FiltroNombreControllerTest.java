package com.columbia.viajes.controller;

import com.columbia.viajes.mapper.CiudadMapper;
import com.columbia.viajes.mapper.HotelMapper;
import com.columbia.viajes.mapper.PaqueteMapper;
import com.columbia.viajes.model.Ciudad;
import com.columbia.viajes.model.Hotel;
import com.columbia.viajes.model.Paquete;
import com.columbia.viajes.repository.*;
import com.columbia.viajes.service.CiudadService;
import com.columbia.viajes.service.HotelService;
import com.columbia.viajes.service.PaqueteService;
import java.util.List;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mapstruct.factory.Mappers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class FiltroNombreControllerTest {

    @ParameterizedTest
    @ValueSource(strings = {"paquetes", "ciudades", "hoteles"})
    void filtroLlegaAlRepositorioSinEspaciosYConservaLaRespuesta(String recurso) throws Exception {
        Escenario escenario = preparar(recurso);
        doReturn(List.of(escenario.entidad())).when(escenario.repository())
                .findByNombreContainingIgnoreCase("CoStA");

        escenario.mvc().perform(get("/api/" + recurso).param("nombre", "  CoStA  "))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].nombre").value("Costa Azul"));

        verify(escenario.repository()).findByNombreContainingIgnoreCase("CoStA");
        verify(escenario.repository(), never()).findAll();
    }

    @ParameterizedTest
    @ValueSource(strings = {"paquetes", "ciudades", "hoteles"})
    void parametroAusenteVacioOConEspaciosDevuelveListadoCompleto(String recurso) throws Exception {
        Escenario escenario = preparar(recurso);
        doReturn(List.of(escenario.entidad())).when(escenario.repository()).findAll();

        escenario.mvc().perform(get("/api/" + recurso))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Costa Azul"));
        for (String nombre : List.of("", "   ")) {
            escenario.mvc().perform(get("/api/" + recurso).param("nombre", nombre))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].nombre").value("Costa Azul"));
        }

        verify(escenario.repository(), times(3)).findAll();
        verify(escenario.repository(), never()).findByNombreContainingIgnoreCase(anyString());
    }

    @ParameterizedTest
    @ValueSource(strings = {"paquetes", "ciudades", "hoteles"})
    void sinCoincidenciasDevuelve200ConListaVacia(String recurso) throws Exception {
        Escenario escenario = preparar(recurso);
        doReturn(List.of()).when(escenario.repository()).findByNombreContainingIgnoreCase("inexistente");

        escenario.mvc().perform(get("/api/" + recurso).param("nombre", "inexistente"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));

        verify(escenario.repository(), never()).findAll();
    }

    private Escenario preparar(String recurso) {
        return switch (recurso) {
            case "paquetes" -> {
                PaqueteRepository repository = mock(PaqueteRepository.class, CALLS_REAL_METHODS);
                Paquete entidad = new Paquete();
                entidad.setNombre("Costa Azul");
                PaqueteService service = new PaqueteService(repository, mock(SucursalRepository.class),
                        mock(VueloRepository.class), mock(HotelRepository.class), Mappers.getMapper(PaqueteMapper.class));
                yield escenario(new PaqueteController(service), repository, entidad);
            }
            case "ciudades" -> {
                CiudadRepository repository = mock(CiudadRepository.class, CALLS_REAL_METHODS);
                Ciudad entidad = new Ciudad();
                entidad.setNombre("Costa Azul");
                CiudadService service = new CiudadService(repository, Mappers.getMapper(CiudadMapper.class));
                yield escenario(new CiudadController(service), repository, entidad);
            }
            case "hoteles" -> {
                HotelRepository repository = mock(HotelRepository.class, CALLS_REAL_METHODS);
                Hotel entidad = new Hotel();
                entidad.setNombre("Costa Azul");
                HotelService service = new HotelService(repository, mock(CiudadRepository.class),
                        Mappers.getMapper(HotelMapper.class));
                yield escenario(new HotelController(service), repository, entidad);
            }
            default -> throw new IllegalArgumentException("Recurso de prueba desconocido: " + recurso);
        };
    }

    private Escenario escenario(Object controller, RepositorioConNombre<?> repository, Object entidad) {
        return new Escenario(MockMvcBuilders.standaloneSetup(controller).build(), repository, entidad);
    }

    private record Escenario(MockMvc mvc, RepositorioConNombre<?> repository, Object entidad) {}
}

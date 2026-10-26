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
import com.columbia.viajes.service.ImagenService;
import java.util.List;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.CsvSource;
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
        simularConsulta(escenario, "CoStA", null, null, List.of(escenario.entidad()));

        escenario.mvc().perform(get("/api/" + recurso).param("nombre", "  CoStA  "))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].nombre").value("Costa Azul"));

        verificarConsulta(escenario, "CoStA", null, null);
        verify(escenario.repository(), never()).findAll();
    }

    @ParameterizedTest
    @ValueSource(strings = {"paquetes", "ciudades", "hoteles"})
    void parametroAusenteVacioOConEspaciosDevuelveListadoCompleto(String recurso) throws Exception {
        Escenario escenario = preparar(recurso);
        simularConsulta(escenario, null, null, null, List.of(escenario.entidad()));
        simularConsulta(escenario, "", null, null, List.of(escenario.entidad()));

        escenario.mvc().perform(get("/api/" + recurso))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Costa Azul"));
        for (String nombre : List.of("", "   ")) {
            escenario.mvc().perform(get("/api/" + recurso).param("nombre", nombre))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].nombre").value("Costa Azul"));
        }

        if (escenario.repository() instanceof CiudadRepository) {
            verify(escenario.repository(), times(3)).findAll();
        } else {
            verificarConsulta(escenario, null, null, null);
            if (escenario.repository() instanceof PaqueteRepository paquetes) {
                verify(paquetes, times(2)).buscar("", null, null);
            } else if (escenario.repository() instanceof HotelRepository hoteles) {
                verify(hoteles, times(2)).buscar("", null);
            }
            verify(escenario.repository(), never()).findAll();
        }
        verify(escenario.repository(), never()).findByNombreContainingIgnoreCase(anyString());
    }

    @ParameterizedTest
    @ValueSource(strings = {"paquetes", "ciudades", "hoteles"})
    void sinCoincidenciasDevuelve200ConListaVacia(String recurso) throws Exception {
        Escenario escenario = preparar(recurso);
        simularConsulta(escenario, "inexistente", null, null, List.of());

        escenario.mvc().perform(get("/api/" + recurso).param("nombre", "inexistente"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));

        verify(escenario.repository(), never()).findAll();
        verificarConsulta(escenario, "inexistente", null, null);
    }

    @ParameterizedTest
    @CsvSource(value = {
        "paquetes,CoStA,4,1",
        "paquetes,NULL,4,1",
        "paquetes,NULL,4,NULL",
        "paquetes,NULL,NULL,1",
        "paquetes,CoStA,4,NULL",
        "paquetes,CoStA,NULL,1",
        "hoteles,CoStA,4,NULL",
        "hoteles,NULL,4,NULL"
    }, nullValues = "NULL")
    void filtrosIndividualesYCombinadosLleganEnElOrdenCorrecto(
            String recurso, String nombre, Integer idCiudad, Integer idHotel) throws Exception {
        Escenario escenario = preparar(recurso);
        simularConsulta(escenario, nombre, idCiudad, idHotel, List.of(escenario.entidad()));
        var solicitud = get("/api/" + recurso);
        if (nombre != null) solicitud.param("nombre", nombre);
        if (idCiudad != null) solicitud.param("idCiudad", idCiudad.toString());
        if (idHotel != null) solicitud.param("idHotel", idHotel.toString());

        escenario.mvc().perform(solicitud)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].nombre").value("Costa Azul"));

        verificarConsulta(escenario, nombre, idCiudad, idHotel);
        verify(escenario.repository(), never()).findAll();
    }

    @ParameterizedTest
    @CsvSource(value = {
        "paquetes,999,999", "paquetes,4,999", "paquetes,999,1", "hoteles,999,NULL"
    }, nullValues = "NULL")
    void consultaCombinadaSinCoincidenciasDevuelveListaVaciaSinListarTodo(
            String recurso, Integer idCiudad, Integer idHotel) throws Exception {
        Escenario escenario = preparar(recurso);
        simularConsulta(escenario, "CoStA", idCiudad, idHotel, List.of());
        var solicitud = get("/api/" + recurso).param("nombre", "CoStA")
                .param("idCiudad", idCiudad.toString());
        if (idHotel != null) solicitud.param("idHotel", idHotel.toString());

        escenario.mvc().perform(solicitud)
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));

        verificarConsulta(escenario, "CoStA", idCiudad, idHotel);
        verify(escenario.repository(), never()).findAll();
    }

    @ParameterizedTest
    @ValueSource(strings = {"paquetes", "hoteles"})
    void nombreConEspaciosNoDesactivaElFiltroDeCiudad(String recurso) throws Exception {
        Escenario escenario = preparar(recurso);
        simularConsulta(escenario, "", 4, null, List.of(escenario.entidad()));

        escenario.mvc().perform(get("/api/" + recurso).param("nombre", "   ").param("idCiudad", "4"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        verificarConsulta(escenario, "", 4, null);
        verify(escenario.repository(), never()).findAll();
    }

    @ParameterizedTest
    @CsvSource({"paquetes,idCiudad", "paquetes,idHotel", "hoteles,idCiudad"})
    void idNoNumericoSeRechazaAntesDeConsultar(String recurso, String parametro) throws Exception {
        Escenario escenario = preparar(recurso);

        escenario.mvc().perform(get("/api/" + recurso).param(parametro, "no-es-un-numero"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(escenario.repository());
    }

    private void simularConsulta(Escenario escenario, String nombre, Integer idCiudad,
            Integer idHotel, List<?> resultado) {
        if (escenario.repository() instanceof PaqueteRepository paquetes) {
            doReturn(resultado).when(paquetes).buscar(nombre, idCiudad, idHotel);
        } else if (escenario.repository() instanceof HotelRepository hoteles) {
            doReturn(resultado).when(hoteles).buscar(nombre, idCiudad);
        } else if (nombre == null || nombre.isBlank()) {
            doReturn(resultado).when(escenario.repository()).findAll();
        } else {
            doReturn(resultado).when(escenario.repository()).findByNombreContainingIgnoreCase(nombre);
        }
    }

    private void verificarConsulta(Escenario escenario, String nombre, Integer idCiudad, Integer idHotel) {
        if (escenario.repository() instanceof PaqueteRepository paquetes) {
            verify(paquetes).buscar(nombre, idCiudad, idHotel);
        } else if (escenario.repository() instanceof HotelRepository hoteles) {
            verify(hoteles).buscar(nombre, idCiudad);
        } else {
            verify(escenario.repository()).findByNombreContainingIgnoreCase(nombre);
        }
    }

    private Escenario preparar(String recurso) {
        return switch (recurso) {
            case "paquetes" -> {
                PaqueteRepository repository = mock(PaqueteRepository.class, CALLS_REAL_METHODS);
                Paquete entidad = new Paquete();
                entidad.setNombre("Costa Azul");
                PaqueteService service = new PaqueteService(repository, mock(SucursalRepository.class),
                        mock(VueloRepository.class), mock(HotelRepository.class), Mappers.getMapper(PaqueteMapper.class),
                        mock(ImagenService.class));
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

package com.columbia.viajes;

import com.columbia.viajes.security.service.JwtService;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import java.sql.Statement;
import tools.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.*;

/** Prueba explícita contra MySQL local: crea datos temporales y los retira al terminar. */
@EnabledIfSystemProperty(named = "pruebas.http.local", matches = "true")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ReservasClienteHttpTest {
    private static final String RUTA = "/api/paquetes-reservas";
    @Value("${local.server.port}") private int puerto;
    @Autowired private JdbcTemplate db;
    @Autowired private JwtService jwt;
    private final HttpClient cliente = HttpClient.newHttpClient();
    private final ObjectMapper json = new ObjectMapper();
    private Integer clienteTemporal;
    private Integer reservaTemporal;
    private List<Map<String, Object>> reservasOriginales;
    private final List<Integer> reservasTemporales = new ArrayList<>();
    private final List<Integer> paquetesTemporales = new ArrayList<>();
    private final List<Integer> usuariosTemporales = new ArrayList<>();

    @Test
    void eliminacionRespetaRolesYSucursalDelPaquetePorHttpReal() throws Exception {
        reservasOriginales = db.queryForList("SELECT * FROM paquetes_reservas ORDER BY id");
        assertFalse(reservasOriginales.isEmpty(), "Se necesita una reserva local como referencia");
        var referencia = reservasOriginales.getFirst();
        int paqueteOriginal = ((Number) referencia.get("id_paquete")).intValue();
        int sucursal = db.queryForObject("SELECT id_sucursal FROM paquetes WHERE id=?", Integer.class, paqueteOriginal);
        int otraSucursal = db.queryForObject("SELECT MIN(id) FROM sucursales WHERE id<>?", Integer.class, sucursal);
        int vendedor = crearUsuarioTemporal("Vendedor", sucursal);
        int companero = crearUsuarioTemporal("Vendedor", sucursal);
        int vendedorAjeno = crearUsuarioTemporal("Vendedor", otraSucursal);
        int sinSucursal = crearUsuarioTemporal("Vendedor", null);
        int clienteId = crearUsuarioTemporal("Cliente", null);
        int administrador = crearUsuarioTemporal("Administrador", null);
        int dueno = crearUsuarioTemporal("Dueño", null);
        int paqueteAjeno = insertar("""
                INSERT INTO paquetes(id_sucursal,id_vuelo_ida,id_vuelo_vuelta,id_hotel,nombre,
                    descripcion,fecha_llegada_hotel,fecha_partida_hotel,precio)
                SELECT ?,id_vuelo_ida,id_vuelo_vuelta,id_hotel,'Prueba DELETE temporal',
                    descripcion,fecha_llegada_hotel,fecha_partida_hotel,precio FROM paquetes WHERE id=?
                """, otraSucursal, paqueteOriginal);
        paquetesTemporales.add(paqueteAjeno);
        int propia = crearReservaTemporal(paqueteOriginal, companero, referencia);
        int ajena = crearReservaTemporal(paqueteAjeno, vendedorAjeno, referencia);
        var antes = db.queryForList("SELECT * FROM paquetes_reservas ORDER BY id");
        int inexistente = db.queryForObject("SELECT MAX(id)+1 FROM paquetes_reservas", Integer.class);
        for (int id : List.of(propia, ajena)) {
            eliminarPorHttp(id, token(clienteId), 403);
            eliminarPorHttp(id, token(sinSucursal), 403);
            eliminarPorHttp(id, null, 401);
            eliminarPorHttp(id, "invalido", 401);
        }
        eliminarPorHttp(ajena, token(vendedor), 404);
        for (int actor : List.of(vendedor, administrador, dueno)) {
            eliminarPorHttp(inexistente, token(actor), 404);
        }
        assertEquals(antes, db.queryForList("SELECT * FROM paquetes_reservas ORDER BY id"),
                "Ningún rechazo debe modificar las reservas");

        // La reserva es de un compañero: el alcance depende del paquete, no de quién vendió.
        eliminarPorHttp(propia, token(vendedor), 204);
        eliminarPorHttp(propia, token(vendedor), 404);
        for (int actor : List.of(administrador, dueno)) {
            for (int paquete : List.of(paqueteOriginal, paqueteAjeno)) {
                int vendedorReserva = paquete == paqueteOriginal ? companero : vendedorAjeno;
                int id = crearReservaTemporal(paquete, vendedorReserva, referencia);
                eliminarPorHttp(id, token(actor), 204);
            }
        }
        eliminarPorHttp(ajena, token(vendedorAjeno), 204);
        assertEquals(reservasOriginales, db.queryForList("SELECT * FROM paquetes_reservas ORDER BY id"));
    }

    private int crearUsuarioTemporal(String rol, Integer sucursal) {
        int id = insertar("""
                INSERT INTO usuarios(nombre,contrasenia,id_rol,id_sucursal,version_autenticacion)
                SELECT ?, 'sin-login-de-prueba', id, ?, 0 FROM roles WHERE nombre=?
                """, "delete_" + UUID.randomUUID().toString().replace("-", ""), sucursal, rol);
        usuariosTemporales.add(id);
        return id;
    }

    private int crearReservaTemporal(int paquete, int vendedor, Map<String, Object> referencia) {
        int id = insertar("""
                INSERT INTO paquetes_reservas(id_paquete,id_turista,clase_vuelo,regimen_hospedaje,precio_reserva,id_vendedor)
                VALUES (?, ?, 'TURISTA', 'MEDIA_PENSION', 100, ?)
                """, paquete, referencia.get("id_turista"), vendedor);
        reservasTemporales.add(id);
        return id;
    }

    private void eliminarPorHttp(int id, String token, int estado) throws Exception {
        String cuerpo = pedir("DELETE", RUTA + "/" + id, token, null, estado);
        if (estado == 204) {
            assertTrue(cuerpo.isEmpty());
            assertEquals(0, db.queryForObject("SELECT COUNT(*) FROM paquetes_reservas WHERE id=?", Integer.class, id));
        }
    }

    @Test
    void aislamientoDeClientesYBloqueoDeEscrituraPorHttpReal() throws Exception {
        reservasOriginales = db.queryForList("SELECT * FROM paquetes_reservas ORDER BY id");
        assertFalse(reservasOriginales.isEmpty(), "Se necesita una reserva local como referencia");
        var referencia = reservasOriginales.getFirst();
        int propietario = ((Number) referencia.get("id_turista")).intValue();
        assertEquals("Cliente", db.queryForObject("SELECT r.nombre FROM usuarios u JOIN roles r ON r.id=u.id_rol WHERE u.id=?", String.class, propietario));
        String nombre = "http_" + UUID.randomUUID().toString().replace("-", "");
        clienteTemporal = insertar("INSERT INTO usuarios(nombre,contrasenia,id_rol,version_autenticacion) SELECT ?, 'sin-login-de-prueba', id, 0 FROM roles WHERE nombre='Cliente'", nombre);
        db.update("INSERT INTO turistas(id,nombre,apellidos,direccion,email,telefono) VALUES (?, 'Prueba', 'HTTP', 'Temporal', ?, '000')", clienteTemporal, nombre + "@example.invalid");
        reservaTemporal = insertar("INSERT INTO paquetes_reservas(id_paquete,id_turista,clase_vuelo,regimen_hospedaje,precio_reserva,id_vendedor) SELECT id_paquete, ?, clase_vuelo,regimen_hospedaje,precio_reserva,id_vendedor FROM paquetes_reservas WHERE id=?", clienteTemporal, referencia.get("id"));
        var antes = db.queryForList("SELECT * FROM paquetes_reservas ORDER BY id");
        String token = token(clienteTemporal);
        String tokenAjeno = token(propietario);
        String detalle = RUTA + "/" + reservaTemporal;

        var listado = json.readTree(pedir("GET", RUTA, token, null, 200));
        assertEquals(1, listado.size());
        assertEquals(reservaTemporal.intValue(), listado.get(0).get("id").asInt());
        assertEquals(clienteTemporal.intValue(), listado.get(0).get("turistaId").asInt());
        assertEquals(reservaTemporal.intValue(), json.readTree(pedir("GET", detalle, token, null, 200)).get("id").asInt());
        pedir("GET", RUTA + "/" + referencia.get("id"), token, null, 404);
        pedir("GET", detalle, tokenAjeno, null, 404);
        var listadoAjeno = json.readTree(pedir("GET", RUTA, tokenAjeno, null, 200));
        assertFalse(listadoAjeno.isEmpty());
        for (var reserva : listadoAjeno) {
            assertEquals(propietario, reserva.get("turistaId").asInt());
            assertNotEquals(reservaTemporal.intValue(), reserva.get("id").asInt());
        }
        int inexistente = db.queryForObject("SELECT MAX(id) + 1 FROM paquetes_reservas", Integer.class);
        pedir("GET", RUTA + "/" + inexistente, token, null, 404);
        String cuerpo = json.writeValueAsString(Map.of("idPaquete", referencia.get("id_paquete"), "idTurista", clienteTemporal,
                "claseVuelo", "TURISTA", "regimenHospedaje", "MEDIA_PENSION", "idVendedor", referencia.get("id_vendedor")));
        for (String metodo : List.of("POST", "PUT", "DELETE")) {
            String ruta = metodo.equals("POST") ? RUTA : detalle;
            String datos = metodo.equals("DELETE") ? null : cuerpo;
            pedir(metodo, ruta, token, datos, 403);
            pedir(metodo, ruta, null, datos, 401);
        }
        for (String ruta : List.of(RUTA, detalle)) {
            pedir("GET", ruta, null, null, 401);
            pedir("GET", ruta, "invalido", null, 401);
        }
        assertEquals(antes, db.queryForList("SELECT * FROM paquetes_reservas ORDER BY id"), "Los rechazos deben conservar todas las reservas");
    }

    private int insertar(String sql, Object... argumentos) {
        var clave = new GeneratedKeyHolder();
        db.update(conexion -> {
            var sentencia = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            for (int i = 0; i < argumentos.length; i++) sentencia.setObject(i + 1, argumentos[i]);
            return sentencia;
        }, clave);
        return clave.getKey().intValue();
    }

    private String token(int id) {
        var usuario = db.queryForMap("SELECT nombre,version_autenticacion FROM usuarios WHERE id=?", id);
        return jwt.generarToken((String) usuario.get("nombre"), ((Number) usuario.get("version_autenticacion")).longValue());
    }

    private String pedir(String metodo, String ruta, String token, String cuerpo, int esperado) throws Exception {
        var solicitud = HttpRequest.newBuilder(URI.create("http://localhost:" + puerto + ruta))
                .timeout(java.time.Duration.ofSeconds(15));
        if (token != null) solicitud.header("Authorization", "Bearer " + token);
        if (cuerpo != null) solicitud.header("Content-Type", "application/json");
        solicitud.method(metodo, cuerpo == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(cuerpo));
        var respuesta = cliente.send(solicitud.build(), HttpResponse.BodyHandlers.ofString());
        assertEquals(esperado, respuesta.statusCode(), metodo + " " + ruta);
        return respuesta.body();
    }

    @AfterEach
    void limpiarYVerificarDatosOriginales() {
        try {
            for (int id : reservasTemporales) db.update("DELETE FROM paquetes_reservas WHERE id=?", id);
            for (int id : paquetesTemporales) db.update("DELETE FROM paquetes WHERE id=?", id);
            for (int id : usuariosTemporales) db.update("DELETE FROM usuarios WHERE id=?", id);
            if (reservaTemporal != null) db.update("DELETE FROM paquetes_reservas WHERE id=?", reservaTemporal);
        } finally {
            if (clienteTemporal != null) {
                db.update("DELETE FROM turistas WHERE id=?", clienteTemporal);
                db.update("DELETE FROM usuarios WHERE id=?", clienteTemporal);
                assertEquals(0, db.queryForObject("SELECT COUNT(*) FROM usuarios WHERE id=?", Integer.class, clienteTemporal));
            }
        }
        if (reservasOriginales != null) assertEquals(reservasOriginales, db.queryForList("SELECT * FROM paquetes_reservas ORDER BY id"));
    }
}

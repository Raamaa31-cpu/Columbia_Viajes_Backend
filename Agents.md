# AGENTS.md — Columbia Viajes Backend

## Proyecto en un vistacillo
- **Java 21**, **Spring Boot 4.1.1**, **Maven** wrapper (`mvnw`)
- Base de datos **MySQL**, **JPA/Hibernate**, **MapStruct 1.5.5**, **Lombok**
- Aplicación Spring Boot de un solo módulo. No es un monorepo.

## Ejecución local
```sh
# desde la raíz del proyecto
./mvnw spring-boot:run
```
El **MySQL** debe estar corriendo en `localhost:3307` con la base de datos `columbia_viajes` y las credenciales `root` / `root1234` (ver `src/main/resources/application.properties`).

## Compilación y testing
```sh
./mvnw compile            # compila el main (ejecuta los procesadores de anotaciones de MapStruct + Lombok)
./mvnw test               # corre los tests (solo existe ViajesApplicationTests — smoke test de contextLoads)
./mvnw package            # buildea el jar (sin frontend)
```

## Arquitectura y organización de paquetes (`com.columbia.viajes`)
- `model` — entidades JPA + enums (nunca se llama `entity`)
- `repository` — interfaces de Spring Data JPA
- `service` — lógica de negocio; límites transaccionales con `@Transactional`
- `controller` — controladores REST; excepciones globales manejadas por `ManejadorErrores`
- `dto.request` / `dto.response` — DTOs estrictamente separados
- `mapper` — interfaces de MapStruct (`componentModel = "spring"`)

## Convenciones clave
- Los controladores devuelven `ResponseEntity<T>` para los endpoints CRUD (no objetos crudos).
- Los `Mappers` son beans de Spring; los servicios los reciben con `@RequiredArgsConstructor`.
- Las validaciones de campo único se hacen en la **capa de servicio** (lanzan `IllegalArgumentException`), no en el controlador. `ManejadorErrores` lo convierte en HTTP 400.
- `ddl-auto=validate` — se espera que el esquema ya exista; la aplicación NO crea ni modifica tablas.
- `Usuario` tiene `@CreationTimestamp` en `fechaCreacion` → los mappers de mapstruct deben usar `@Mapping(target = "fechaCreacion", ignore = true)` en los métodos de escritura.
- Los DTOs son `record`s de Java con anotaciones de `jakarta.validation` (`@NotBlank`, `@NotNull`, `@Size`, `@Min`).
- Los comentarios y mensajes del código están en **español (argentino)**.

## Endpoints (módulos en progreso)
| Módulo  | Ruta del controlador | Estado       |
|---------|----------------------|--------------|
| Ciudad  | `/api/ciudades`      | ✅ Completo  |
| Sucursal| `/api/sucursales`    | ✅ Completo  |
| Hotel   | `/api/hoteles`       | ✅ Completo  |
| Rol     | `/api/roles`         | ✅ Completo  |
| Vuelo   | `/api/vuelos`        | ✅ Completo  |
| Usuario | `/api/usuarios`      | ✅ Completo  |
| Paquete | `/api/paquetes`      | ✅ Completo  |
| Turista | `/api/turistas`      | ✅ Completo  |
| PaqueteReserva | `/api/paquetes-reservas` | ✅ Completo |
| PaqueteReserva | `/api/paquetes-reservas` | ✅ Completo |

## Trucos y advertencias
- Lombok + MapStruct juntos requieren el procesador de anotaciones `lombok-mapstruct-binding` en `pom.xml` (ya configurado).
- No existe `application-test.properties` — los tests de integración que necesiten DB fallarán sin MySQL.
- El directorio `target/` está en el `.gitignore`; los artefactos compilados (`HotelMapperImpl.class`, etc.) se generan en tiempo de build.
- **Modo enseñanza:** Cuando se agregue o modifique algo relacionado con funcionalidades nuevas (ej. SpringSecurity), explicar paso a paso qué se hizo y qué hace cada cosa. No asumir conocimiento previo del framework.

# Flujo de Trabajo (Workflow)
- **Memoria del Proyecto:** Antes de comenzar a escribir código, proponer soluciones o iniciar una nueva tarea, debes leer SIEMPRE el archivo `Memory.md` para conocer el estado actual del proyecto y no repetir trabajo ya hecho.
- **Actualización:** Cuando terminemos de implementar una funcionalidad importante y funcione correctamente, recuérdame actualizar el `Memory.md` para tachar la tarea.
- **Principio DRY (No te repitas) ♻️:** Evitá el código duplicado a toda costa. Si notás que un fragmento de lógica o un bloque de código se repite en dos o más lugares (por ejemplo, validaciones comunes o cálculos), refactorizalo automáticamente extrayéndolo a un método privado reutilizable o a una clase utilitaria.

# Base de Datos y Relaciones
- Base de datos: MySQL. Revisar schema.sql al añadir o modificar clases que se relacionen con la base de datos.
- Estrategia JPA: Usamos @Entity y @Table para mapear.
- Relaciones clave: Un Turista puede tener muchas Reservas. Un Paquete incluye Vuelo y Hotel.
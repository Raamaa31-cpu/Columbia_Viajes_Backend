# ✈️ Columbia Viajes — Backend

API REST para gestionar una agencia de viajes: sucursales, destinos, hoteles, vuelos, paquetes turísticos, usuarios, turistas y reservas.

Proyecto de aprendizaje en desarrollo, con autenticación mediante JWT, permisos por rol y restricciones de reservas por sucursal. La aplicación es un único módulo de Spring Boot; este repositorio contiene el backend.

## 🧰 Tecnologías

| Tecnología | Uso |
| --- | --- |
| Java 21 | Lenguaje y entorno de ejecución |
| Spring Boot 4.1.1 | Aplicación y API REST |
| Spring Security | Autenticación y autorización |
| Spring Data JPA / Hibernate | Persistencia y relaciones |
| MySQL 8.0 | Base de datos; versión usada para el archivo SQL |
| JJWT 0.12.5 | Emisión y validación de JWT con HS256 |
| BCrypt | Hash de contraseñas |
| MapStruct 1.5.5.Final | Conversión entre entidades y DTOs |
| Lombok | Generación de constructores y métodos |
| Jakarta Validation | Validación de solicitudes |
| Maven Wrapper | Compilación y ejecución |

## 🚀 Ejecución local

### 1. Requisitos

- JDK 21 y `JAVA_HOME` configurado.
- MySQL 8.0, accesible por defecto en `localhost:3307`.
- Git para clonar el repositorio.
- Conexión a Internet para descargar las dependencias en la primera ejecución.
- Postman o un cliente HTTP para utilizar la API.

Cloná el repositorio y ejecutá los comandos desde su carpeta raíz. Maven está incluido mediante `mvnw` y `mvnw.cmd`.

### 2. Preparar MySQL

Desde la raíz del proyecto, abrí el cliente de MySQL con tu usuario:

```powershell
mysql --host=127.0.0.1 --port=3307 --user=tu_usuario_mysql --password
```

Dentro del cliente, creá y seleccioná la base, y cargá el archivo:

```sql
CREATE DATABASE IF NOT EXISTS columbia_viajes
CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;

USE columbia_viajes;
SOURCE schema.sql;
```

**La importación de `schema.sql` reemplaza las tablas existentes de la base seleccionada.** Usalo para preparar una base de desarrollo que pueda reemplazarse.

El archivo contiene la estructura de nueve tablas y una copia de datos de prueba: ciudades, roles, sucursales y usuarios con hashes BCrypt. Las tablas de hoteles, vuelos, paquetes, turistas y reservas están vacías en esa copia. También incluye `usuarios.version_autenticacion`, necesario para la revocación de tokens.

Hibernate usa `ddl-auto=validate`: comprueba que las tablas coincidan con las entidades, pero no las crea ni las modifica. El archivo SQL tampoco se actualiza automáticamente cuando cambian los datos de MySQL.

### 3. Configurar la conexión y el secreto JWT

La configuración está en [application.properties](src/main/resources/application.properties). Podés reemplazar la conexión y sus credenciales mediante variables de entorno de Spring Boot:

```powershell
$env:SPRING_DATASOURCE_URL = "jdbc:mysql://localhost:3307/columbia_viajes"
$env:SPRING_DATASOURCE_USERNAME = "tu_usuario_mysql"
$env:SPRING_DATASOURCE_PASSWORD = "tu_clave_mysql"
```

`JWT_SECRET` es obligatorio: debe contener una clave aleatoria codificada en Base64, de al menos 32 bytes antes de codificarla. Para generar una en PowerShell, compatible con Windows PowerShell 5.1:

```powershell
$jwtBytes = New-Object byte[] 32
$jwtGenerator = [System.Security.Cryptography.RandomNumberGenerator]::Create()
try {
    $jwtGenerator.GetBytes($jwtBytes)
} finally {
    $jwtGenerator.Dispose()
}
$env:JWT_SECRET = [Convert]::ToBase64String($jwtBytes)
```

Estas variables quedan disponibles en la terminal actual y en los procesos que inicies desde ella. Usá un secreto estable si querés conservar la validez de los tokens entre reinicios: generar otro secreto invalida los JWT firmados con el anterior. Mantené las claves reales fuera del repositorio.

| Configuración | Valor por defecto / requisito |
| --- | --- |
| Base de datos | `jdbc:mysql://localhost:3307/columbia_viajes` |
| Esquema JPA | `validate` |
| Secreto JWT | Variable de entorno `JWT_SECRET` |
| Duración del JWT | `900000` ms: 15 minutos |
| URL de la API | `http://localhost:8080` |

### 4. Iniciar la aplicación

En la misma terminal de PowerShell:

```powershell
.\mvnw.cmd spring-boot:run
```

En Linux o macOS, configurá las variables de entorno en tu shell y ejecutá:

```bash
./mvnw spring-boot:run
```

La API queda disponible en `http://localhost:8080`. Si ejecutás desde un IDE, configurá también `JWT_SECRET` y las credenciales de MySQL en el entorno del proceso que inicia la aplicación.

## 🔐 Autenticación

### Cuentas de prueba

El SQL incluye cuentas como `prueba_admin`, `prueba_duenio` y vendedores asociados a sucursales. Sus contraseñas están almacenadas como hashes; para iniciar sesión necesitás conocer la contraseña original correspondiente. La antigua cuenta Cliente `prueba_security` se eliminó porque no tenía un Turista asociado.

El registro público crea únicamente cuentas Cliente. Para configurar el acceso inicial de gestión, si desconocés las contraseñas de las cuentas importadas, asigná una contraseña propia a una cuenta de prueba usando un hash BCrypt generado con `BCryptPasswordEncoder`. No hay recuperación de contraseña implementada. Por ejemplo, en un entorno Java con las dependencias del proyecto:

```java
new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder()
        .encode("TU_CONTRASENIA_DE_PRUEBA");
```

Para asignar el resultado a la cuenta de administrador importada, ejecutá en tu base de desarrollo, reemplazando el marcador por el hash generado:

```sql
UPDATE usuarios
SET contrasenia = 'HASH_BCRYPT_GENERADO',
    version_autenticacion = version_autenticacion + 1
WHERE nombre = 'prueba_admin';
```

### Registro de Cliente

```http
POST /api/auth/registro
Content-Type: application/json
```

```json
{
  "nombreUsuario": "ana_viajes",
  "contrasenia": "TU_CONTRASENIA_DE_PRUEBA",
  "turista": {
    "nombre": "Ana",
    "apellidos": "Pérez",
    "direccion": "Av. Siempre Viva 123",
    "email": "ana@example.com",
    "telefono": "1123456789"
  }
}
```

En Postman, usá **No Auth**. El servidor fija el rol Cliente y guarda Usuario y Turista juntos en una transacción. Exige nombre de usuario y email disponibles, valida los campos del perfil y guarda la contraseña como hash BCrypt. Devuelve `201 Created` con `UsuarioResponse`; para obtener un JWT, hacé login después del registro. El login por email todavía está pendiente: por ahora se usa el nombre de usuario.

Desde gestión, `POST /api/usuarios` recibe `UsuarioRequest`: `nombre`, `contrasenia`, `idRol`, `idSucursal` y `turista`. El perfil `turista` es obligatorio si el rol es Cliente y debe omitirse para otros roles. El backend obtiene al creador de la autenticación y valida sus permisos antes de guardar.

### Login

```http
POST /api/auth/login
Content-Type: application/json
```

```json
{
  "nombre": "prueba_admin",
  "contrasenia": "TU_CONTRASENIA_DE_PRUEBA"
}
```

Respuesta `200 OK`:

```json
{
  "nombre": "prueba_admin",
  "rolNombre": "Administrador",
  "token": "JWT_GENERADO"
}
```

Enviá el token en las solicitudes protegidas:

```http
Authorization: Bearer JWT_GENERADO
```

En Postman, elegí **Authorization → Bearer Token**. Para iniciar sesión, usá **No Auth**: un Bearer inválido enviado al login también puede ser rechazado por el filtro.

### Cambio de contraseña

```http
POST /api/auth/cambiar-contrasenia
Authorization: Bearer JWT_GENERADO
Content-Type: application/json
```

```json
{
  "contraseniaActual": "TU_CONTRASENIA_ACTUAL",
  "contraseniaNueva": "TU_NUEVA_CONTRASENIA"
}
```

- Disponible para cualquier usuario autenticado; la cuenta se obtiene de la autenticación.
- La contraseña actual se comprueba con `PasswordEncoder.matches()`.
- La nueva debe tener entre 8 y 64 caracteres y se guarda como hash BCrypt.
- Un cambio correcto devuelve `204 No Content`; una contraseña actual incorrecta devuelve `400`.
- Se incrementa `version_autenticacion` y se rechazan todos los JWT anteriores de esa cuenta, incluidos los de otros dispositivos. Es necesario volver a iniciar sesión para obtener un token con la nueva versión.

La API usa autenticación sin sesión HTTP (`STATELESS`). En cada solicitud con JWT, verifica la firma y el vencimiento, carga la cuenta y el rol actual desde MySQL, y compara la versión del token con la del usuario. Los tokens sin el claim `versionAutenticacion` también se rechazan.

### Cierre de sesión en todos los dispositivos

```http
POST /api/auth/logout
Authorization: Bearer JWT_GENERADO
```

La solicitud no lleva cuerpo. En Postman, elegí **Body → none**.

El endpoint obtiene al usuario autenticado, aumenta `version_autenticacion` y devuelve `204 No Content`. Todos los JWT anteriores de esa cuenta quedan revocados, incluido el utilizado para cerrar sesión y los de otros dispositivos. La contraseña se conserva; un nuevo login permite obtener otro JWT.

El frontend debe eliminar también su copia del token al finalizar. Este logout tiene alcance global; no hay cierre individual por dispositivo. Conocer la versión o editarla dentro del JWT no permite acceder: una modificación del contenido invalida la firma verificada por el servidor.

## 👥 Permisos actuales

Cada usuario tiene un único rol: `Administrador`, `Dueño`, `Vendedor` o `Cliente`.

| Operación | Sin sesión | Cliente | Vendedor | Administrador / Dueño |
| --- | --- | --- | --- | --- |
| Consultar paquetes, ciudades, hoteles, vuelos y sucursales | Sí | Sí | Sí | Sí |
| Registrarse como Cliente con perfil de Turista | Sí | Sí | Sí | Sí |
| Crear, editar y eliminar datos del catálogo | No | No | No | Sí |
| Consultar turistas | No | No | Todos | Todos |
| Crear perfil junto con Cliente, editar y eliminar turistas | No | No | No | Sí |
| Consultar vendedores | No | No | De su sucursal | Todos |
| Listar cuentas Cliente | No | No | Todas, con sucursal asignada | Sí |
| Consultar usuarios y gestionar cuentas según jerarquía | No | No | No | Sí, con las restricciones indicadas abajo |
| Consultar roles | No | No | No | Sí |
| Consultar, crear, editar y eliminar reservas | No | No | De su sucursal | De todas las sucursales |
| Cambiar su propia contraseña | No | Sí | Sí | Sí |
| Cerrar todas las sesiones de su cuenta | No | Sí | Sí | Sí |

Dueño puede crear, editar y eliminar usuarios de cualquier rol. Administrador solo puede gestionar cuentas Cliente/Vendedor y asignar esos roles; no puede editar ni eliminar una cuenta Administrador/Dueño. Ambos conservan acceso a la gestión del catálogo. El ranking de vendedores por ventas para el Dueño está pendiente.

La edición general de usuarios no permite convertir entre Cliente y roles de gestión: el perfil se crea con la cuenta y se conserva. Los cambios de rol dentro de las cuentas de gestión respetan la jerarquía del actor.

El Vendedor puede gestionar reservas de compañeros de su misma sucursal. Al crear una reserva queda asignado como vendedor automáticamente; al editarla conserva al vendedor original.

## 📡 Endpoints

Todas las rutas usan el prefijo `/api`.

| Recurso | Ruta base | Operaciones disponibles |
| --- | --- | --- |
| Ciudades | `/api/ciudades` | Listar, obtener, crear, actualizar y eliminar |
| Sucursales | `/api/sucursales` | Listar, obtener, crear, actualizar y eliminar |
| Hoteles | `/api/hoteles` | Listar, obtener, crear, actualizar y eliminar |
| Vuelos | `/api/vuelos` | Listar, obtener, crear, actualizar y eliminar |
| Paquetes | `/api/paquetes` | Listar, obtener, crear, actualizar y eliminar |
| Usuarios | `/api/usuarios` | Listar, obtener, crear, actualizar y eliminar |
| Turistas | `/api/turistas` | Listar, obtener, actualizar y eliminar; se crean junto con la cuenta Cliente |
| Reservas | `/api/paquetes-reservas` | Listar, obtener, crear, actualizar y eliminar |
| Roles | `/api/roles` | Listar y obtener |

Los recursos CRUD siguen estas rutas:

| Método | Ruta | Operación |
| --- | --- | --- |
| `GET` | `/api/{recurso}` | Listar |
| `GET` | `/api/{recurso}/{id}` | Obtener un registro |
| `POST` | `/api/{recurso}` | Crear |
| `PUT` | `/api/{recurso}/{id}` | Actualizar |
| `DELETE` | `/api/{recurso}/{id}` | Eliminar |

Rutas adicionales:

| Método | Ruta | Operación |
| --- | --- | --- |
| `POST` | `/api/auth/login` | Autenticar y obtener un JWT |
| `POST` | `/api/auth/registro` | Registro público de Cliente y Turista juntos |
| `POST` | `/api/auth/cambiar-contrasenia` | Cambiar la contraseña propia y revocar JWT anteriores |
| `POST` | `/api/auth/logout` | Cerrar todas las sesiones de la cuenta autenticada |
| `GET` | `/api/usuarios/vendedores` | Listar vendedores según el alcance del rol |
| `GET` | `/api/usuarios/clientes` | Listar cuentas Cliente; Administrador/Dueño o Vendedor con sucursal |

Los GET de catálogo son públicos. El resto de los permisos se detalla en la tabla anterior. Para los campos de cada solicitud y respuesta, consultá [dto/request](src/main/java/com/columbia/viajes/dto/request) y [dto/response](src/main/java/com/columbia/viajes/dto/response).

Los perfiles de turistas se crean junto con la cuenta Cliente mediante registro público o creación de gestión. No existe un POST independiente de turistas. El JSON de `TuristaRequest` contiene únicamente `nombre`, `apellidos`, `direccion`, `email` y `telefono`. Al editar mediante `PUT /api/turistas/{id}`, se conserva el Usuario relacionado.

## 🧳 Reservas y cálculo del precio

Un paquete pertenece a una sucursal e incluye hotel, vuelo de ida y vuelo de vuelta. Un turista está vinculado a un usuario y puede tener varias reservas. Cada reserva relaciona un paquete, un turista y un vendedor.

El email del turista es único en MySQL. El service valida duplicados al crear y editar, permitiendo conservar el email de la propia cuenta y rechazando el de otro turista con un error de datos. El registro de cuenta y perfil está implementado; queda pendiente su verificación con MySQL y el login por email.

Ejemplo de solicitud de reserva:

```json
{
  "idPaquete": 1,
  "idTurista": 1,
  "claseVuelo": "PRIMERA",
  "regimenHospedaje": "PENSION_COMPLETA",
  "idVendedor": 4
}
```

Los IDs son ilustrativos: deben existir en la base. Para Administrador/Dueño, `idVendedor` es obligatorio y debe pertenecer a la sucursal del paquete. Al crear como Vendedor, ese campo se ignora y se utiliza la cuenta autenticada.

`precioReserva` se calcula en el servidor y no se recibe en el JSON:

```text
precioReserva = precioPaquete × (1 + adicionalHospedaje + adicionalVuelo)
```

| Hospedaje | Clase de vuelo | Adicional total |
| --- | --- | --- |
| `MEDIA_PENSION` | `TURISTA` | 0 % |
| `PENSION_COMPLETA` | `TURISTA` | 10 % |
| `MEDIA_PENSION` | `PRIMERA` | 10 % |
| `PENSION_COMPLETA` | `PRIMERA` | 20 % |

Se utiliza `BigDecimal` y se redondea al final a dos decimales con `HALF_UP`. Al editar una reserva se recalcula con el precio actual del paquete. Cambiar el precio de un paquete no modifica automáticamente las reservas ya guardadas.

## 🗂️ Organización del código

Los paquetes están bajo `com.columbia.viajes`:

| Paquete | Responsabilidad |
| --- | --- |
| `model` | Entidades JPA y enums |
| `repository` | Acceso a MySQL mediante Spring Data JPA |
| `service` | Reglas de negocio y transacciones |
| `controller` | Endpoints REST y manejo global de errores |
| `dto.request` / `dto.response` | Datos de entrada y salida separados |
| `mapper` | Mappers de MapStruct registrados como beans de Spring |
| `security` / `security.service` | Configuración, autenticación, contexto de usuario y JWT |

Los DTOs son records de Java. Lombok y MapStruct usan procesadores de anotaciones; su integración está configurada con `lombok-mapstruct-binding` en el `pom.xml`.

## 🧪 Compilación y pruebas

Comandos para PowerShell:

```powershell
.\mvnw.cmd compile
.\mvnw.cmd "-Dtest=PaqueteReservaServiceTest" test
.\mvnw.cmd "-Dtest=TuristaServiceTest" test
.\mvnw.cmd "-Dtest=RegistroClienteServiceTest" test
.\mvnw.cmd "-Dtest=AuthRegistroControllerTest" test
.\mvnw.cmd test
.\mvnw.cmd package
```

En Linux/macOS, reemplazá `.\mvnw.cmd` por `./mvnw`.

- `compile`: compila el código y genera los mappers.
- `PaqueteReservaServiceTest`: contiene 12 pruebas unitarias de reservas; usa mocks y no necesita MySQL ni `JWT_SECRET`.
- `TuristaServiceTest`: contiene nueve pruebas unitarias de unicidad de email, conservación de la relación al editar, rechazo de perfiles duplicados y creación únicamente para un Cliente existente; usa mocks y no necesita MySQL ni `JWT_SECRET`.
- `RegistroClienteServiceTest`: contiene 32 casos de registro, perfiles obligatorios, jerarquía de creación/edición/eliminación y conservación de contraseña/versión. Usa mocks; todavía falta verificar la reversión real de ambos guardados con MySQL.
- `AuthRegistroControllerTest`: contiene tres pruebas MockMvc standalone de respuesta 201 sin contraseña y validación del perfil anidado. No utiliza MySQL ni la cadena de filtros de seguridad.
- `test`: incluye `ViajesApplicationTests.contextLoads`, que necesita MySQL, el esquema preparado y `JWT_SECRET`. No hay un perfil de integración separado.
- `package`: ejecuta las pruebas y genera `target/viajes-0.0.1-SNAPSHOT.jar`.

Con la configuración de entorno disponible, el JAR se ejecuta mediante:

```powershell
java -jar target/viajes-0.0.1-SNAPSHOT.jar
```

**Verificaciones registradas:** compilación exitosa; pruebas unitarias de reservas aprobadas; pruebas manuales en Postman de login, autorización por sucursal, cambio de contraseña y revocación de JWT anteriores. También se confirmó el logout global: acceso con token válido → 200, logout → 204 y acceso con el token anterior → 401; sin token → 401; nuevo login con la misma contraseña y acceso con el nuevo JWT → 200. La cobertura HTTP de todas las operaciones todavía está pendiente.

## 🚧 Estado y pendientes

- Completar pruebas de logout con otros roles y otros tokens de la cuenta.
- Definir renovación de JWT y permisos adicionales del Cliente.
- Revisar CORS para integrar el frontend.
- Implementar el endpoint del ranking de vendedores para el Dueño.
- Verificar por HTTP el registro público, los dos guardados y su rollback por errores con MySQL. Implementar login por email para Clientes.
- Verificar por HTTP la creación conjunta desde gestión y la jerarquía de creación/edición/eliminación implementada en el service.
- Verificar por HTTP el listado de clientes para Vendedor; el matcher de `/api/usuarios/clientes` ya está corregido.
- Verificar por HTTP la conservación del Usuario al editar perfiles de turista.
- Completar pruebas HTTP de creación, edición, precios y eliminación de reservas, y de tokens vencidos y cuentas eliminadas.
- Investigar un caso observado de respuesta `401` en lugar de un error de datos esperado durante el despacho interno de errores. El comportamiento HTTP de eliminación con `ResponseStatusException` sigue pendiente de verificación.

El avance y las verificaciones detalladas se registran en [Memory.md](Memory.md). Las convenciones y el flujo de trabajo del proyecto están en [Agents.md](Agents.md).

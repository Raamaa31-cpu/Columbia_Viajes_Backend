# Estado del Proyecto - Columbia Viajes

## Estado Actual
- Estructura base del proyecto configurada con Java 21, Spring Boot y Maven.
- Archivo de reglas de IA (`Agents.md`) implementado.
- Módulos de Sucursal, Vuelo, Hotel, Ciudad, Usuario, Turista, Paquetes y PaqueteReserva completos.
- Spring Security en implementación guiada paso a paso. Autenticación con MySQL y BCrypt, JWT, autorización por rol, restricciones por sucursal en reservas y cambio de contraseña con revocación de JWT anteriores implementados. Quedan pruebas completas por HTTP y funcionalidades de seguridad pendientes.

## Avance de Seguridad — 07/10/2026
- `PasswordConfig` registra un `PasswordEncoder` con BCrypt. La creación de usuarios guarda el hash de la contraseña.
- La edición de datos usa `UsuarioActualizacionRequest`, sin contraseña. `UsuarioMapper.actualizarUsuario()` ignora `contrasenia` para conservar el hash existente.
- La validación de sucursal se comparte entre creación y actualización mediante `obtenerSucursalVendedor(Integer idSucursal, Rol rol)`.
- `UserDetailsServiceImpl` busca usuarios por nombre en MySQL y entrega el hash y el nombre del único rol como autoridad de Spring Security.
- `AuthenticationConfig` configura `DaoAuthenticationProvider` y un `AuthenticationManager` basado en `ProviderManager`.
- `POST /api/auth/login` recibe `AuthRequest`. `AuthService` autentica antes de emitir el token. `AuthResponse` devuelve nombre, rolNombre y token, sin contraseña ni hash.
- `ManejadorErrores` responde HTTP 401 con "Credenciales incorrectas" ante `BadCredentialsException`.
- `JwtService` usa JJWT y HS256. Obtiene la clave Base64 de `app.jwt.secret=${JWT_SECRET}`; la clave se configura fuera de Git como variable de entorno. `app.jwt.expiration-ms=900000` establece 15 minutos de validez por token.
- Los tokens incluyen usuario (`subject`), emisión y vencimiento. La lectura verifica firma y vencimiento, y exige usuario y fecha de expiración.
- `JwtAuthenticationFilter` lee `Authorization: Bearer <token>`, valida el JWT, carga la cuenta y su rol actual desde MySQL y registra la autenticación en `SecurityContextHolder` para esa solicitud. Tokens inválidos o usuarios inexistentes se rechazan con 401.
- El filtro se construye en `SecurityConfig` y se agrega con `addFilterBefore()`, sin `@Component` ni registro independiente como bean de filtro.
- Configuración sin sesión HTTP (`STATELESS`), sin form login ni HTTP Basic. CSRF desactivado para el diseño con credenciales enviadas explícitamente en Authorization; revisar esta decisión si se incorpora autenticación por cookies.
- Son públicos el POST de login y los GET de listado y detalle de paquetes, ciudades, hoteles, vuelos y sucursales. Las rutas de gestión están restringidas por rol (ver avance del 11/10).
- Sin autenticación en una ruta protegida, el `authenticationEntryPoint` devuelve 401 con "Necesitás iniciar sesión". El filtro devuelve 401 sin cuerpo ante tokens inválidos.

### Verificación realizada
- Compilaciones con `mvnw.cmd compile` exitosas durante la implementación.
- Pruebas manuales en Postman realizadas por el usuario: ciudades sin autenticación → 200 con datos; usuarios sin token → 401 con mensaje.
- Login con usuario inexistente o contraseña incorrecta → 401 con "Credenciales incorrectas".
- Login correcto de la cuenta local `prueba_security`, rol Cliente y contraseña almacenada como hash BCrypt → 200 con nombre, rol y JWT.
- Antes de configurar restricciones por rol: GET de usuarios con JWT válido → respuesta con usuarios; token alterado → 401; sin token → 401 con "Necesitás iniciar sesión". Actualmente un Cliente no puede consultar usuarios: recibe 403.
- Pendientes de verificar: token vencido y acceso con token de una cuenta eliminada. Las pruebas anteriores no certifican autorización por rol ni seguridad completa.

## Reglas de Acceso Confirmadas
- Cada usuario tiene un único rol.
- Administrador y Dueño pueden realizar todas las operaciones; rutas configuradas en SecurityConfig.
- Vendedor puede gestionar reservas de su sucursal, consultar vendedores de esa sucursal y consultar todos los turistas. Las demás entidades solo se consultan; no tiene acceso al CRUD general de usuarios.
- Requisito específico del Dueño: consultar un ranking de vendedores según sus ventas. Existe la consulta en PaqueteReservaRepository con SUM(precioReserva); exposición y verificación del endpoint pendientes.
- Permisos adicionales del Cliente pendientes de definir; el rol Cliente no tiene acceso a las rutas de gestión restringidas.

## Avance de Reservas y Seguridad — 11/10/2026
- UsuarioActualService obtiene el usuario autenticado desde el contexto de seguridad y MySQL, sin confiar en una identidad enviada en el JSON.
- GET /api/vendedores devuelve todos los vendedores para Administrador/Dueño y solo los de la sucursal del Vendedor. Turistas se consultan sin filtro por sucursal.
- Respuestas de autorización verificadas manualmente durante la enseñanza: Cliente sin permiso recibe 403; accesos por sucursal al listar y obtener reservas fueron probados por el usuario.
- buscarReservaAccesible devuelve Optional<PaqueteReserva> y se reutiliza en obtener, actualizar y eliminar. Administrador/Dueño buscan por ID; Vendedor busca por ID y sucursal del paquete. En obtener/actualizar, reserva inexistente o fuera de alcance se convierte en 404 en el controlador.
- Al crear, resolverVendedorCreacion exige idVendedor para Administrador/Dueño. Para Vendedor se asigna el usuario autenticado y se ignora idVendedor enviado.
- validarVendedorDelPaquete comprueba rol Vendedor, sucursal asignada y coincidencia con la sucursal del paquete. Sucursal incompatible genera 403 para el Vendedor autenticado y 400 para Administrador/Dueño.
- Al actualizar como Vendedor, se conserva el vendedor original de la reserva y se comprueba que el nuevo paquete pertenezca a la sucursal del usuario autenticado. Administrador/Dueño pueden seleccionar otro vendedor compatible.
- precioReserva fue eliminado de PaqueteReservaRequest. El mapper ignora ese campo en escritura; el service lo calcula al crear y actualizar.
- Cálculo: precio actual del paquete × (1 + 0.10 por PENSION_COMPLETA + 0.10 por PRIMERA). Ambos adicionales suman 20 %, sin multiplicarlos sucesivamente. BigDecimal, dos decimales y RoundingMode.HALF_UP al final.
- Al editar se recalcula con el precio actual del paquete; las reservas guardadas no cambian automáticamente cuando cambia el precio del paquete.
- schema.sql ya define precio y precio_reserva como DECIMAL(12,2); no se modificó el esquema.
- PaqueteReservaServiceTest: 12 pruebas unitarias aprobadas sin MySQL, incluyendo restricciones de sucursal, conservación del vendedor, asignación automática, las cuatro combinaciones de precio, recálculo y redondeo. Verificación completa de creación/actualización/precios por HTTP pendiente de confirmación.
- ManejadorErrores maneja MethodArgumentNotValidException con HTTP 400 y una lista de mensajes por campo.
- El usuario implementó eliminar conservando void y @Transactional: obtiene el usuario autenticado, consulta buscarReservaAccesible y lanza ResponseStatusException(HttpStatus.NOT_FOUND, "No se encontro la reserva.") si no hay una reserva accesible. Solo llama a delete cuando la encuentra. El controlador conserva ResponseEntity<Void> y devuelve 204 cuando el service termina correctamente. Comportamiento HTTP de eliminación pendiente de probar.
- Preferencia explícita: conservar void en eliminar y lanzar la excepción dentro del service; no cambiarlo a boolean.
- Verificación final del 11/10: mvnw.cmd -Dtest=PaqueteReservaServiceTest test compiló el código actual, incluida la eliminación, y aprobó las 12 pruebas existentes. Esas pruebas todavía no incluyen eliminación ni verifican estados HTTP.

## Cambio de Contraseña y Revocación de JWT — 11/10/2026
- El usuario escribió el código como práctica; se revisó por pasos pequeños con ejemplos y preguntas de comprensión.
- POST /api/auth/cambiar-contrasenia está en AuthController y requiere autenticación mediante anyRequest().authenticated(). Está disponible para todos los roles autenticados.
- CambioContraseniaRequest recibe contraseniaActual y contraseniaNueva, sin ID ni nombre para elegir una cuenta. Ambas tienen @NotBlank; solo la nueva exige entre 8 y 64 caracteres con @Size, siguiendo UsuarioRequest.
- UsuarioService.cambiarContrasenia devuelve void y usa @Transactional. Obtiene la cuenta mediante UsuarioActualService, comprueba la contraseña actual con passwordEncoder.matches(), lanza IllegalArgumentException si no coincide y guarda el hash BCrypt de la nueva mediante encode(). El controlador devuelve 204 sin cuerpo cuando termina correctamente; ManejadorErrores convierte la contraseña actual incorrecta en 400.
- El usuario agregó en MySQL usuarios.version_autenticacion BIGINT NOT NULL DEFAULT 0. Se confirmó que los seis usuarios existentes empezaron en 0. Usuario lo mapea mediante un campo long versionAutenticacion y @Column(name = "version_autenticacion", nullable = false).
- UsuarioMapper ignora versionAutenticacion tanto en creación como en actualización. El contador es interno y no puede elegirse desde los DTOs.
- Al cambiar la contraseña correctamente, el service aumenta versionAutenticacion en uno antes de save(), dentro de la misma transacción que guarda el nuevo hash.
- UsuarioAutenticado extiende org.springframework.security.core.userdetails.User y conserva una copia de la versión de autenticación cargada desde MySQL. UserDetailsServiceImpl devuelve esa clase. AuthService toma el principal después de authenticate() y pasa su nombre y versión a JwtService.generarToken().
- Los nuevos JWT incluyen el claim versionAutenticacion, además del subject y las fechas. DatosToken es un record interno con nombreUsuario y versionAutenticacion.
- JwtService.leerToken verifica y lee el JWT una sola vez; exige usuario, vencimiento y una versión numérica no negativa. Usa Long para detectar una versión ausente y devuelve DatosToken. Los tokens anteriores a la incorporación del claim también se rechazan por no incluirlo; este caso específico no se probó por HTTP.
- JwtAuthenticationFilter carga el usuario actual desde MySQL y compara ambas versiones antes de construir la autenticación. Si difieren, lanza JwtException; el catch limpia el contexto y devuelve 401 sin cuerpo.
- Cambiar la contraseña invalida todos los JWT anteriores de esa cuenta, incluidos los de otros dispositivos. No se implementó todavía un endpoint de cierre de sesión ni una revocación individual por dispositivo o token.
- schema.sql se exportó desde la base local con nueve tablas y quince registros: tres ciudades, cuatro roles, dos sucursales y seis usuarios. Después se agregó version_autenticacion a la definición y el valor 0 a los seis INSERT de usuarios. Es una foto de los datos al exportar; los cambios de contraseña y versión posteriores no actualizan el archivo automáticamente.

### Verificación realizada
- mvnw.cmd compile terminó con BUILD SUCCESS después de completar el versionado y el filtro. No se agregaron ni ejecutaron pruebas automatizadas nuevas para este flujo.
- Pruebas manuales realizadas y confirmadas por el usuario en Postman: contraseña actual incorrecta → 400; login con la contraseña original todavía funciona; cambio correcto → 204; login con la contraseña anterior → 401; login con la nueva → 200 y un JWT nuevo.
- Revocación comprobada con una cuenta de Administrador de prueba: GET /api/usuarios con token A → 200; cambio de contraseña con ese token → 204; repetir el GET con el token A original, antes de su vencimiento → 401.
- Recuperación de acceso comprobada: login con la contraseña nueva → 200; GET /api/usuarios con el nuevo token B → 200.

## Tareas Pendientes (Backlog)
- [x] Implementar el módulo completo de Usuario (Controller, Service, Mapper, DTOs).
- [x] Implementar el módulo completo de Turista (Controller, Service, Mapper, DTOs).
- [x] Implementar el módulo completo de Paquetes (Controller, Service, Mapper, DTOs).
- [x] Implementar el módulo de PaqueteReserva (Controller, Service, Mapper, DTOs).
- [ ] Implementar Spring Security (JWT auth, SecurityConfig, UserDetailsService, AuthController).
- [x] Implementar y probar autenticación con MySQL y BCrypt.
- [x] Implementar y probar emisión y autenticación mediante JWT (casos válido, alterado y ausente).
- [ ] Configurar y probar autorización por rol: Administrador y Dueño con acceso completo; definir permisos de Cliente y Vendedor.
- [x] Configurar las rutas por rol para Administrador, Dueño y Vendedor.
- [x] Restringir listado y detalle de reservas por sucursal; verificados manualmente.
- [x] Implementar creación y actualización de reservas con validación de vendedor y sucursal; pruebas unitarias aprobadas.
- [x] Calcular precioReserva en el servidor con adicionales sumados y redondeo a dos decimales.
- [ ] Confirmar creación, actualización y cálculo de precios en Postman.
- [x] Implementar eliminación de reservas con búsqueda restringida por sucursal, void y excepción en el service.
- [ ] Probar eliminación por HTTP: sucursal propia 204, ajena o ID inexistente 404; Administrador/Dueño pueden eliminar en cualquier sucursal; Cliente 403 y sin token 401. Comprobar que un rechazo no elimina la reserva.
- [ ] Verificar rechazo de tokens vencidos y de cuentas eliminadas.
- [x] Implementar y probar cambio de contraseña del usuario autenticado, con comprobación de contraseña actual.
- [x] Implementar y probar revocación de todos los JWT anteriores de la cuenta mediante versionAutenticacion al cambiar la contraseña.
- [ ] Definir e implementar cierre de sesión: distinguir revocación del token actual y revocación de todos los tokens de la cuenta.
- [ ] Definir renovación de JWT y revisar CORS para el frontend.
- [ ] Corregir la regla del listado de clientes para Vendedor: el controlador expone /api/usuarios/clientes, pero SecurityConfig permite /api/clientes; actualmente la ruta real queda restringida a Administrador/Dueño.
- [ ] Implementar ranking de vendedores según sus ventas para el Dueño.

## Problemas o Bugs Conocidos
- Completar las pruebas manuales y definir permisos adicionales del Cliente antes de considerar completa la seguridad.
- Revisar el manejo de errores si un 404/400 esperado termina en 401 al pasar por el despacho interno de errores. Ya se observó un 401 durante una creación de sucursal con datos inválidos; causa del despacho no confirmada con una prueba de integración. SecurityConfig no tiene una regla explícita para DispatcherType.ERROR. No dar por probado el 404 HTTP de ResponseStatusException solo por revisar el código.
- El listado de cuentas Cliente tiene una ruta distinta en SecurityConfig y UsuarioController. Aunque el service contempla al Vendedor con sucursal, la regla general /api/usuarios/** lo bloquea antes de llegar al service. No confundir este listado con la consulta de turistas, que sí está habilitada para Vendedor.

## Continuación en Otro Chat
1. Leer Agents.md y este Memory.md, y revisar el código actual antes de proponer cambios.
2. Retomar la enseñanza por cierre de sesión. El cambio de contraseña y la revocación por versión ya están implementados y probados manualmente. Explicar primero la diferencia entre salir de un dispositivo y revocar todos los tokens de la cuenta; el alcance del logout todavía no se decidió.
3. Completar las pruebas manuales de DELETE /api/paquetes-reservas/{id}. Usar reservas de prueba de dos sucursales, confirmar estados y ausencia de borrado al rechazar. Si aparece 401 en vez de 404, investigar el despacho de errores.
4. Confirmar en Postman creación, actualización y precios: 0/10/20 % sobre el precio base; al editar una reserva de un compañero conservar vendedorId; impedir cambio a un paquete de otra sucursal.
5. Verificar token vencido y token de usuario eliminado; corregir la ruta del listado de clientes para Vendedor, definir permisos adicionales del Cliente, revisar rutas sin regla específica (actualmente anyRequest().authenticated()), y decidir renovación y CORS cuando corresponda.
6. Retomar el ranking del Dueño después: existe consulta por monto vendido, falta endpoint/DTO y pruebas. Revisar también que los turistas estén vinculados a usuarios con rol Cliente.

### Preferencias de Enseñanza
- Explicar en español argentino, desde cero, lento y por pasos pequeños, con emojis y ejemplos concretos.
- El usuario prefiere implementar los ejercicios y pedir revisión. Modificar su código cuando lo pida explícitamente; en este chat pidió ayuda directa para separar los métodos y proteger actualización.
- Hacer preguntas de comprensión cuando sean útiles, sin cantidad fija. No avanzar varios temas juntos.
- Se explicaron BigDecimal, factor inicial 1 (=100 %), adicional 0.10 y setScale(2, HALF_UP). Ejemplo correcto: 25.678 se redondea a 25.68.
- Usa NetBeans en Windows 10 y Postman; no registrar JWT_SECRET, tokens ni credenciales nuevas en esta memoria.

# Estado del Proyecto - Columbia Viajes

## Estado Actual
- Estructura base del proyecto configurada con Java 21, Spring Boot y Maven.
- Archivo de reglas de IA (`Agents.md`) implementado.
- Módulos de Sucursal, Vuelo, Hotel, Ciudad, Usuario, Turista, Paquetes y PaqueteReserva completos.
- Spring Security en implementación guiada paso a paso. Autenticación con MySQL y BCrypt, JWT, autorización por rol, restricciones por sucursal en reservas, cambio de contraseña y logout global con revocación de JWT anteriores implementados. Quedan pruebas completas por HTTP y funcionalidades de seguridad pendientes.

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
- Dueño puede gestionar usuarios de cualquier rol. Administrador puede crear, editar y eliminar únicamente cuentas Cliente/Vendedor y asignar esos roles; los controles del service cubren origen y destino al editar. Los dos roles conservan acceso a la gestión del catálogo. Ver implementación de registro y jerarquía del 13/10; falta prueba HTTP de la nueva jerarquía.
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
- Cambiar la contraseña invalida todos los JWT anteriores de esa cuenta, incluidos los de otros dispositivos. Posteriormente se agregó el logout global (ver avance del 12/10); no hay revocación individual por dispositivo o token.
- schema.sql se exportó desde la base local con nueve tablas y quince registros: tres ciudades, cuatro roles, dos sucursales y seis usuarios. Después se agregó version_autenticacion a la definición y el valor 0 a los seis INSERT de usuarios. Es una foto de los datos al exportar; los cambios de contraseña y versión posteriores no actualizan el archivo automáticamente.

### Verificación realizada
- mvnw.cmd compile terminó con BUILD SUCCESS después de completar el versionado y el filtro. No se agregaron ni ejecutaron pruebas automatizadas nuevas para este flujo.
- Pruebas manuales realizadas y confirmadas por el usuario en Postman: contraseña actual incorrecta → 400; login con la contraseña original todavía funciona; cambio correcto → 204; login con la contraseña anterior → 401; login con la nueva → 200 y un JWT nuevo.
- Revocación comprobada con una cuenta de Administrador de prueba: GET /api/usuarios con token A → 200; cambio de contraseña con ese token → 204; repetir el GET con el token A original, antes de su vencimiento → 401.
- Recuperación de acceso comprobada: login con la contraseña nueva → 200; GET /api/usuarios con el nuevo token B → 200.

## Cierre de Sesión Global — 12/10/2026
- El usuario eligió la alternativa más simple: cerrar todas las sesiones de la cuenta mediante el contador existente, sin implementar revocación individual por token.
- POST /api/auth/logout está en AuthController, no recibe cuerpo y obtiene la cuenta a través de UsuarioActualService en el service. Requiere un JWT válido mediante anyRequest().authenticated(); está habilitado por configuración para todos los roles autenticados.
- UsuarioService.cerrarTodasLasSesiones devuelve void y usa @Transactional. Aumenta versionAutenticacion y guarda el usuario, sin modificar la contraseña. El controlador devuelve 204 sin cuerpo.
- El usuario escribió inicialmente cerrarSesion(); a su pedido, se renombró a cerrarTodasLasSesiones y se extrajo incrementarVersionAutenticacion(Usuario usuario), un método privado compartido con cambiarContrasenia. Cada método público conserva su transacción y save(). El endpoint se agregó luego con ayuda directa.
- SecurityConfig mantiene desactivado el logout automático de Spring; el endpoint REST propio implementa la revocación para el diseño STATELESS. El frontend deberá eliminar su copia del token al cerrar sesión.
- Se explicó que la versión no es un secreto: cambiar el contenido de un JWT invalida su firma. JWT_SECRET se utiliza para firmar al emitir y verificar al recibir; conocer el contador no permite generar un token válido. El usuario comprendió la diferencia entre verificar la firma y comparar la versión.
- Compilación mvnw.cmd compile exitosa después del refactor y de agregar el endpoint. No se agregaron ni ejecutaron pruebas automatizadas nuevas para logout.
- Prueba manual confirmada por el usuario en Postman, con una cuenta de Administrador de prueba: GET /api/usuarios con JWT válido → 200; POST /api/auth/logout con ese JWT → 204; repetir el GET con el mismo token antes de vencer → 401.
- Verificación adicional del 12/10: el usuario mostró en Postman logout sin token → 401 con "Necesitás iniciar sesión" y confirmó nuevo login con la misma contraseña → 200 y acceso a /api/usuarios con el nuevo JWT → 200.
- Todavía falta confirmar por HTTP: funcionamiento con Cliente/Vendedor/Dueño y rechazo de otros tokens de la misma cuenta tras el logout. Se propuso una prueba con tokens A y B, pero el usuario prefirió continuar con el siguiente tema; no registrar esa prueba como realizada.
- README.md actualizado con el endpoint, su alcance global, permisos y verificación realizada.

## Registro de Clientes — Decisiones y Preparación — 12/10/2026
- El usuario decidió continuar con registro de nuevos usuarios. Se acordó la jerarquía: Dueño puede crear cualquier rol; Administrador solo Vendedor y Cliente; Vendedor y Cliente no pueden crear cuentas de gestión. La restricción debe cubrir también cambios de rol y todavía falta implementarla.
- El registro público debe asignar Cliente desde el servidor y crear su Turista en la misma transacción, con la relación uno a uno y el ID compartido mediante @MapsId. No recibir un rol o un ID ajeno para elegir la cuenta del registro.
- Se solicitó que los Clientes puedan iniciar sesión mediante el email del Turista; los otros roles conservarán el nombre de usuario. Registro y login por email todavía no están implementados. Al buscar por email se deberá exigir rol Cliente y conservar una identidad inequívoca para los JWT.
- A pedido del usuario, se agregó en MySQL el índice UNIQUE uq_turistas_email sobre turistas.email. Se comprobó que no había emails duplicados antes del ALTER. La entidad Turista declara el mismo UniqueConstraint y email como @Column(nullable = false, length = 120).
- TuristaRepository ahora expone findByEmailIgnoreCase. TuristaService comparte validarEmailDisponible(email, idTurista) entre crear y actualizar. Crear rechaza cualquier email ocupado; actualizar permite el propio, incluso con cambios de mayúsculas, y rechaza el de otro turista. Lanza IllegalArgumentException, manejada como HTTP 400. La restricción de MySQL protege también frente a solicitudes concurrentes; las violaciones de integridad se manejan con el 409 existente.
- Se eliminó únicamente la cuenta prueba_security, ID 1 y rol Cliente, que no tenía Turista ni reservas relacionadas. Se verificó una sola fila eliminada y cinco usuarios restantes. No usar esa cuenta en próximos ejercicios; los resultados anteriores quedan como historial de pruebas.
- schema.sql se reexportó desde MySQL el 12/10: nueve tablas y catorce registros (tres ciudades, cuatro roles, dos sucursales y cinco usuarios), con hashes y versiones actuales al exportar, email único y sin el INSERT de la cuenta eliminada.
- TuristaServiceTest incorpora cuatro pruebas unitarias: creación con email ocupado, actualización conservando email propio, actualización con email ajeno sin modificar/guardar y creación con email disponible conservando la relación con Usuario. mvnw.cmd -Dtest=TuristaServiceTest test terminó con BUILD SUCCESS; cuatro pruebas aprobadas sin MySQL. La validación todavía no se probó por HTTP.
- Al revisar el código se encontró que el usuario ya corrigió el matcher de clientes a /api/usuarios/clientes y /api/usuarios/clientes/**, antes de la regla general de usuarios. La corrección está en el código y compiló, pero su prueba HTTP sigue pendiente.

## TuristaRequest Reutilizable — 13/10/2026
- El usuario propuso quitar idUsuario de TuristaRequest para reutilizar el mismo DTO en registro y gestión. No se creó DatosTuristaRequest. TuristaRequest conserva nombre, apellidos, direccion, email y telefono con sus validaciones.
- El usuario quitó el ID del DTO y adaptó la edición: TuristaMapper.actualizarTurista ignora usuario e id y recibe únicamente request y @MappingTarget Turista. TuristaService.actualizar conserva la relación existente, sin consultar UsuarioRepository ni aceptar reasignación.
- A pedido del usuario se completó la creación: TuristaService.crear recibe idUsuario por separado, exige que exista, tenga rol Cliente y todavía no tenga un Turista. Mantiene la validación compartida del email. Un segundo intento de crear el perfil no reemplaza al existente.
- La creación de gestión pasó de POST /api/turistas a POST /api/usuarios/{idUsuario}/turista en UsuarioController, que inyecta TuristaService. Devuelve 201 y recibe solo el perfil en el JSON. La regla actual /api/usuarios/** exige Administrador/Dueño. La edición y demás operaciones siguen bajo /api/turistas.
- Se adaptó TuristaServiceTest al DTO sin ID y se agregaron casos para impedir crear un segundo perfil, asociar roles distintos de Cliente y utilizar un usuario inexistente. También se comprueba que la edición conserva el ID y la misma instancia de Usuario. mvnw.cmd -Dtest=TuristaServiceTest test compiló y aprobó nueve pruebas sin MySQL; las rutas nuevas todavía no se verificaron por HTTP.
- Este refactor no modifica tablas ni datos de MySQL. Registro público, jerarquía de asignación de roles y login por email continúan pendientes.
- Se creó RegistroClienteRequest a pedido del usuario: nombreUsuario (@NotBlank y @Size de 2 a 100), contrasenia (@NotBlank y @Size de 8 a 64) y TuristaRequest turista (@NotNull y @Valid). Reutiliza el perfil de cinco campos y no recibe rol ni IDs. mvnw.cmd compile terminó con BUILD SUCCESS. Todavía falta el service de registro y su endpoint; este DTO por sí solo no habilita el registro público.
- El service transaccional se agregó posteriormente (ver sección siguiente); falta conectar el registro con un endpoint público.

## Método de Registro de Cliente — 13/10/2026
- A pedido del usuario se agregó UsuarioService.registrarCliente(RegistroClienteRequest), con @Transactional y retorno UsuarioResponse. Busca el rol Cliente por nombre mediante el nuevo RolRepository.findByNombre; el ID del rol no está fijado en el código ni se toma del JSON. Si falta el rol configurado, lanza IllegalStateException.
- Construye internamente un UsuarioRequest con las credenciales, el rol Cliente obtenido del servidor y sucursal null. Guarda al Usuario mediante el helper privado crearUsuario y luego llama a TuristaService.crear(idGenerado, request.turista()), que conserva la validación del email y la relación del perfil con la cuenta.
- UsuarioService inyecta TuristaService. Este último participa en la misma transacción del registro; si lanza una excepción de ejecución, esta se propaga y Spring debe revertir ambos guardados. El registro no necesita consultar al usuario autenticado y su respuesta no incluye contraseña ni hash.
- Se extrajo crearUsuario(request, rol, sucursal) para compartir validación de nombre, mapping, encode() y save() entre registro y creación de gestión. validarNombreDisponible(nombre, idUsuario) se comparte también con actualizar y permite conservar el nombre propio. La jerarquía acordada para gestión aún está pendiente; este cambio no la implementa.
- RegistroClienteServiceTest agrega seis pruebas unitarias: rol Cliente obtenido por nombre e ID variable, hash BCrypt y llamada al perfil con el ID generado, nombre ocupado, rol Cliente ausente, propagación de error del perfil y regresiones de creación de gestión/edición conservando hash y versión. Se usa TuristaService simulado; estas pruebas no verifican rollback real de MySQL.
- mvnw.cmd -Dtest=RegistroClienteServiceTest,TuristaServiceTest test compiló y aprobó quince pruebas (seis de registro y nueve de turistas). No se iniciaron pruebas HTTP ni se modificaron datos de MySQL.
- Próximo paso: POST /api/auth/registro en AuthController con @Valid @RequestBody, respuesta 201 y una regla pública limitada a ese POST en SecurityConfig. Después verificar registro completo, duplicados, validación anidada y reversión del Usuario si falla el Turista usando MySQL.

## Endpoint de Registro y Creación Conjunta — 13/10/2026
- Se agregó POST /api/auth/registro en AuthController con @Valid @RequestBody RegistroClienteRequest, llamada a UsuarioService.registrarCliente y respuesta 201 con UsuarioResponse, sin contraseña ni JWT. SecurityConfig permite únicamente los POST exactos de login y registro; cambio de contraseña y logout siguen protegidos.
- UsuarioRequest incorporó @Valid TuristaRequest turista. Para la creación de gestión de Cliente, el service exige perfil; para los demás roles, exige que no se envíe. RegistroClienteRequest conserva @NotNull y @Valid en su perfil.
- crearUsuario ahora centraliza la creación conjunta: valida el perfil según rol, valida nombre, mapea, calcula hash, guarda Usuario y crea Turista si es Cliente. Tanto crear() como registrarCliente() lo invocan dentro de @Transactional. registrarCliente construye el UsuarioRequest interno con rol Cliente del servidor, sucursal null y el perfil, sin una segunda llamada duplicada a TuristaService.
- crear() valida al actor obtenido de UsuarioActualService. El helper validarGestionDeRoles se comparte con actualizar() y eliminar(): Dueño puede gestionar todos; Administrador solo Cliente/Vendedor, tanto como rol objetivo existente como destino; Vendedor/Cliente reciben AccessDeniedException. Esto evita eludir las restricciones de creación mediante edición y protege cuentas privilegiadas al editar/eliminar.
- actualizar() rechaza convertir entre Cliente y roles de gestión, incluso como Dueño, porque ese request no recibe un perfil para crear la relación ni define cómo quitarla. Mantiene los cambios dentro de roles de gestión autorizados y la edición del Cliente conservando su tipo. Esta restricción conserva la relación del perfil; no es una conversión de cuentas implementada.
- El usuario ya había quitado crearTurista() de UsuarioController y su dependencia de TuristaService. No existe el POST independiente /api/usuarios/{idUsuario}/turista ni el antiguo POST /api/turistas. Los perfiles se crean junto con la cuenta; TuristaService.crear sigue disponible internamente. Consulta, edición y eliminación de turistas siguen expuestas.
- RegistroClienteServiceTest se amplió a 32 casos, incluidos los 16 cruces actor/rol de creación, perfiles requeridos/prohibidos, bloqueos de promoción y de edición/eliminación de cuentas privilegiadas por Administrador, y conversión entre tipos de cuenta. AuthRegistroControllerTest agrega tres pruebas MockMvc standalone: 201 sin contraseña, email anidado inválido y perfil ausente con 400 antes del service.
- mvnw.cmd -Dtest=RegistroClienteServiceTest,TuristaServiceTest,AuthRegistroControllerTest test aprobó 44 pruebas con BUILD SUCCESS. MockMvc standalone no incluye la cadena de seguridad ni MySQL: todavía falta verificar el registro anónimo, los dos registros persistidos, el rollback por email duplicado y la jerarquía por HTTP real. No se modificaron tablas ni datos de MySQL en este paso.
- README actualizado con el endpoint, ejemplos, perfiles obligatorios de Clientes y alcance de permisos. Login por email continúa pendiente; por ahora el login sigue usando nombre.
- Verificación manual del 24/10: tras probar el JSON de registro público, el usuario confirmó que pudo registrar un Cliente e iniciar sesión con su nombre de usuario. No confirmó todavía la consulta de los dos registros con ID compartido ni el rollback por email duplicado; esos casos continúan pendientes.

## Login por Email — Primer Paso — 24/10/2026
- Este primer diseño con DTO separado fue reemplazado por decisión del usuario; ver la sección de login unificado siguiente. No volver a crear AuthClienteRequest.
- El usuario pidió comenzar el login por email de Clientes. Se creó AuthClienteRequest en dto.request, con email (@NotBlank, @Email y @Size de máximo 120, de acuerdo con la columna de turistas) y contrasenia (@NotBlank, como el login actual).
- mvnw.cmd compile terminó con BUILD SUCCESS. Este paso solo agrega el DTO; no se modificaron AuthService, UserDetailsServiceImpl ni los endpoints de login, que todavía autentican por nombre. No se modificaron tablas ni datos de MySQL.
- Próximo paso: método de AuthService que reciba AuthClienteRequest, resuelva el email mediante TuristaRepository.findByEmailIgnoreCase, compruebe rol Cliente y reutilice AuthenticationManager y la emisión de AuthResponse/JWT. Mantener la identidad canónica del usuario en el JWT y evitar duplicar la lógica de emisión. Después conectar y probar el endpoint correspondiente.

## Login Unificado por Identificador — 24/10/2026
- El usuario aclaró que un Cliente debe poder iniciar sesión tanto con email como con username en el mismo login. Los demás roles conservarán el acceso por nombre de usuario; la resolución por email debe admitir únicamente rol Cliente.
- A su pedido se eliminó AuthClienteRequest.java. AuthRequest ahora recibe identificador (@NotBlank con mensaje "El email o nombre de usuario no puede estar vacío") y contrasenia (@NotBlank). No lleva @Email porque el identificador también puede ser un nombre de usuario.
- Se adaptó AuthService.autenticar para leer request.identificador() y se actualizó el JSON de login del README. El contrato de POST /api/auth/login usa la clave identificador en lugar de nombre; AuthResponse sigue devolviendo nombre, rolNombre y token.
- mvnw.cmd compile terminó con BUILD SUCCESS. En este paso solo cambió el DTO y su referencia: UserDetailsServiceImpl todavía busca por nombre, por lo que la búsqueda por email no está implementada. No presentar el login por email como terminado ni crear un endpoint separado.
- Próximo paso: resolver el identificador por nombre o por email de un Turista con rol Cliente, evitando confundir cuentas si un nombre coincide con un email. Reutilizar AuthenticationManager y la emisión de JWT con la identidad canónica; mantener la carga del usuario desde el JWT por esa identidad. Mostrar los bloques modificados y avanzar por pasos pequeños.

## Login Unificado Implementado — 24/10/2026
- A pedido del usuario se implementó el mismo POST /api/auth/login con AuthRequest(identificador, contrasenia): Cliente puede ingresar por nombre o por email; los otros roles por nombre. No se creó otro DTO ni endpoint.
- UserDetailsServiceImpl inyecta TuristaRepository y agrega loadUserByIdentificador, con @Transactional(readOnly = true). Consulta nombre y email, admite como candidato por email solo a un Usuario con rol Cliente y devuelve UsuarioAutenticado con los datos de la cuenta resuelta.
- Si el nombre y el email resuelven a IDs diferentes, se rechaza el login. Si ambos apuntan al mismo ID, se permite. El rechazo usa UsernameNotFoundException con mensaje genérico; DaoAuthenticationProvider lo convierte en BadCredentialsException, que el manejador responde como 401. Esta política evita elegir una cuenta arbitrariamente cuando el identificador es ambiguo.
- AuthenticationConfig.authenticationProvider recibe el bean concreto UserDetailsServiceImpl y construye DaoAuthenticationProvider(userDetailsService::loadUserByIdentificador). Sigue usando el PasswordEncoder BCrypt y el AuthenticationManager existentes. La contraseña la verifica Spring Security; no se comparan hashes manualmente ni se emite el JWT antes de autenticar.
- loadUserByUsername conserva su búsqueda exclusivamente por nombre para JwtAuthenticationFilter. No se reinterpreta el subject de un JWT como email. AuthService ya pasaba request.identificador() y sigue emitiendo AuthResponse/JWT con el nombre canónico y la versión de la cuenta autenticada.
- AuthServiceTest agrega 17 casos usando AuthenticationConfig, AuthenticationManager, DaoAuthenticationProvider y BCrypt reales, repositorios y JwtService simulados. Cubre Cliente por nombre/email, gestión por nombre, contraseña incorrecta en ambos identificadores, rechazo de emails asociados a otros roles, cuenta inexistente, coincidencia de nombre/email de la misma cuenta y ambigüedad entre cuentas distintas, y carga canónica del usuario para JWT.
- mvnw.cmd -Dtest=AuthServiceTest test aprobó los 17 casos con BUILD SUCCESS. Se ejecutó además ViajesApplicationTests.contextLoads: cargó correctamente Spring Security, sus beans y JPA contra MySQL con ddl-auto=validate (una prueba aprobada). No se crearon cuentas ni se modificaron tablas o datos en estas verificaciones.
- README actualizado con el contrato identificador, ejemplos por username/email y la política de ambigüedad. Falta confirmar en Postman login Cliente por ambos identificadores, contraseña incorrecta y uso del JWT emitido por email en una ruta autenticada. No confundir esta implementación con las notas históricas que todavía dicen que el email estaba pendiente.

## Validación Cruzada de Username y Email — 24/10/2026
- El usuario pidió impedir que un nuevo username coincida con el email de otra cuenta y viceversa. Se agregó ValidadorIdentificadoresService, que inyecta UsuarioRepository y TuristaRepository y centraliza validarNombreDisponible(nombre, idUsuario) y validarEmailDisponible(email, idUsuario).
- Se movieron al validador las comprobaciones de unicidad anteriores de UsuarioService y TuristaService. Cada método valida su propia tabla y luego la otra; perteneceAOtraCuenta compara IDs y permite conservar valores de la cuenta actual. En una misma cuenta, username y email pueden ser iguales porque no hay ambigüedad de identidad.
- UsuarioService.crearUsuario valida username y, para Cliente, email antes de encode/save. Cubre registro público y creación de gestión. actualizar valida el nuevo username contra ambas tablas. TuristaService.crear y actualizar validan el email contra ambas tablas usando el ID compartido del Usuario/Turista para excluir la cuenta propia.
- Las coincidencias ajenas lanzan IllegalArgumentException, que ManejadorErrores convierte en 400. Los mensajes distinguen username coincidente con email ajeno y email coincidente con username ajeno. El email usa findByEmailIgnoreCase y el username usa findByNombre con la colación actual de MySQL utf8mb4_0900_ai_ci.
- Se agregaron doce casos de ValidadorIdentificadoresServiceTest y cinco a RegistroClienteServiceTest (37 en total), y se amplió TuristaServiceTest a trece casos. Se probaron rechazos en creación/edición, rechazo antes de guardar Usuario y aceptación de valores de la cuenta propia. Se adaptaron los constructores de las pruebas al validador compartido.
- El primer comando con validador, registro, turistas y login aprobó 78 casos. Después de agregar un caso más para crear un perfil con email igual al username propio y reforzar la prueba de conservación de la relación, se ejecutaron TuristaServiceTest (13 aprobados) y ViajesApplicationTests.contextLoads (1 aprobado, con MySQL). Las cuatro clases unitarias suman 79 casos comprobados y el contexto inicia correctamente con las nuevas dependencias.
- Una consulta de solo lectura a MySQL confirmó cero coincidencias preexistentes entre username y email de cuentas con IDs distintos. No se modificaron tablas ni datos ni schema.sql en este paso.
- Son comprobaciones de aplicación; los UNIQUE actuales de MySQL siguen siendo individuales por tabla, sin una restricción global entre ambas. No se implementó serialización de registros concurrentes. Se conserva la defensa que rechaza identificadores ambiguos al iniciar sesión. No afirmar garantía atómica global frente a concurrencia solamente por estas validaciones.
- Próximas pruebas manuales: rechazar al registrar un username igual a email ajeno, un email igual a username ajeno y repetir esas comprobaciones al editar. Los duplicados conocidos ahora se rechazan antes de guardar Usuario; probar ese caso no demuestra un rollback posterior al INSERT. La verificación de rollback real con un fallo posterior a guardar Usuario sigue pendiente.

## Consulta de Reservas del Cliente — 25/10/2026
- Se reutilizan GET /api/paquetes-reservas y GET /api/paquetes-reservas/{id}. Cliente consulta únicamente sus reservas, Vendedor las de la sucursal del paquete y Administrador/Dueño todas.
- listar() incorpora Cliente con findByTurista_Id. obtener() incorpora Cliente con findByIdAndTurista_Id; ambos usan el ID de UsuarioActualService.obtener(), compartido con Turista, sin recibir la identidad del solicitante desde el frontend.
- buscarReservaAccesible conserva únicamente los roles de gestión porque se comparte con actualizar y eliminar. Cliente sigue sin poder editar ni eliminar, incluso al invocar directamente el service.
- SecurityConfig permite a los cuatro roles solo los GET de listado y detalle antes de la restricción general de reservas. El controlador existente convierte Optional.empty() en 404 para reserva inexistente o fuera del alcance del solicitante.
- PaqueteReservaServiceTest aprobó 24 casos sin MySQL: listado por rol, consulta propia o fuera de alcance, consulta por sucursal, acceso de gestión y rechazo de edición/eliminación para Cliente. Compilación aprobada. Las pruebas usan repositorios simulados; falta confirmar por HTTP real el aislamiento entre Clientes, los permisos y los estados 200/404/401/403. No se modificó schema.sql ni MySQL.

## Filtro por Nombre del Catálogo — 25/10/2026
- Esta sección registra la implementación inicial; paquetes y hoteles fueron extendidos después con filtros combinados (ver sección siguiente). La aprobación de 34 pruebas indicada abajo es histórica, anterior a esa extensión.
- GET /api/paquetes, /api/ciudades y /api/hoteles aceptan nombre como query parameter opcional, por ejemplo /api/paquetes?nombre=aventura. Se busca por el nombre propio de cada entidad, con coincidencia parcial y sin distinguir mayúsculas/minúsculas; no se implementaron filtros de paquetes por ciudad/hotel en este paso.
- RepositorioConNombre<T>, marcado @NoRepositoryBean, comparte findByNombreContainingIgnoreCase y el método default listarPorNombre: sin texto devuelve findAll y con texto quita espacios de los extremos con strip antes de consultar. PaqueteRepository, CiudadRepository y HotelRepository lo extienden y conservan sus operaciones existentes. La consulta filtra en la base de datos, sin cargar todo para filtrar en Java.
- Los controladores reciben @RequestParam(name = "nombre", required = false); los services ahora usan listar(String nombre) y conservan el mapping a sus DTOs. Sin parámetro, vacío o solo espacios se conserva el listado completo; sin coincidencias devuelve 200 con []. Los GET conservan acceso público por las reglas existentes de SecurityConfig.
- FiltroNombreControllerTest aprobó nueve casos parametrizados para los tres módulos: transmisión del filtro, eliminación de espacios, parámetro ausente/vacío/espacios y respuesta vacía sin coincidencias. Usa controladores, services, mappers y método default reales con consultas de repositorio simuladas; no certifica resultados de búsqueda contra datos reales de MySQL ni la cadena de seguridad.
- mvnw.cmd -Dtest=FiltroNombreControllerTest,PaqueteReservaServiceTest,ViajesApplicationTests test terminó con BUILD SUCCESS: 34 pruebas aprobadas (9 de filtros, 24 de reservas y 1 de contexto). El contexto inició con MySQL y ddl-auto=validate, reconociendo los repositorios y sus consultas nuevas. No se modificaron tablas ni datos ni schema.sql. README actualizado con ejemplos de filtro y permisos de reservas.

## Filtros Combinados de Hoteles y Paquetes — 25/10/2026
- El usuario implementó los cambios como práctica guiada. GET /api/hoteles admite nombre e idCiudad; GET /api/paquetes admite nombre, idCiudad e idHotel. GET /api/ciudades conserva el filtro por nombre anterior.
- HotelRepository.buscar y PaqueteRepository.buscar usan @Query con JPQL y @Param. Cada filtro es opcional mediante (:parametro IS NULL OR condicion); las condiciones se unen con AND. La búsqueda por nombre usa LOWER y LIKE con CONCAT('%', :nombre, '%'). Los alias y paréntesis de la consulta de paquetes se corrigieron durante la revisión.
- En paquetes, idCiudad filtra p.hotel.ciudad.id e idHotel filtra p.hotel.id. La ciudad representa la del hotel, no la ciudad de origen del vuelo. El orden de argumentos es nombre, idCiudad, idHotel en controlador, service y repositorio.
- Los controladores reciben @RequestParam(required = false). Se corrigió int por Integer en idCiudad tanto en HotelController como en HotelService para permitir omitir la ciudad sin errores de null/unboxing. Los services aplican strip solo cuando nombre no es null y llaman a buscar, conservando el mapper y la respuesta ResponseEntity<List<...>> del controlador.
- Nombre ausente, vacío o solo espacios no restringe por nombre; ciudad/hotel enviados siguen filtrando. Sin filtros se listan todos; sin coincidencias se devuelve 200 con []. No se modificaron permisos, entidades, esquema ni datos de MySQL durante esta implementación.
- Verificación: ViajesApplicationTests aprobó el inicio del contexto con MySQL y las nuevas consultas; la compilación de los cambios de service/controller también terminó con BUILD SUCCESS. Esto no reemplaza las pruebas de resultados ni demuestra aprobación de toda la suite actual.
- El usuario confirmó en Postman el funcionamiento de los filtros de hoteles y que sin filtros devuelve todos. También confirmó las dos consultas propuestas para paquetes: nombre=bariloche&idCiudad=4&idHotel=1 devuelve el paquete de prueba; con idCiudad=1 e igual nombre/hotel devuelve []. El intento previo del agente no pudo conectarse a localhost:8080; estos resultados HTTP son confirmados por el usuario.
- Pendiente: adaptar FiltroNombreControllerTest. Sus simulaciones actuales todavía esperan findByNombreContainingIgnoreCase/findAll para paquetes y hoteles, pero esos servicios ahora llaman a buscar; no registrar esas pruebas como aprobadas para la implementación nueva. Agregar cobertura de combinaciones y parámetros ausentes. README actualizado con parámetros, semántica AND, ejemplos y alcance de verificación.
- Enseñanza realizada: LIKE, comodín %, CONCAT, LOWER, parámetros @Param, filtros opcionales con IS NULL, agrupación con paréntesis, AND, strip con comprobación de null y diferencia entre int/Integer.

## Datos de Prueba para Reservas — 25/10/2026
- A pedido del usuario se cargaron datos ficticios directamente en MySQL local dentro de una transacción: ciudad Bariloche ID 4, Hotel Lago Azul - Prueba ID 1, vuelo de ida ID 1 (CABA-Bariloche, 15/11/2026 08:00 a 10:20) y vuelta ID 2 (Bariloche-CABA, 22/11/2026 18:00 a 20:20).
- Paquete ID 1: Bariloche entre lagos - Prueba, estadía 15/11/2026 a 22/11/2026, precio base 850000.00. Se reutilizó la sucursal existente ID 2 para coincidir con el vendedor existente; no se crearon ni reasignaron vendedores ni sucursales.
- Reserva ID 1 para el Cliente existente juan_viajes (Usuario/Turista ID 7) con vendedor_a1 (ID 4, sucursal 2), clase PRIMERA y PENSION_COMPLETA. Precio guardado 1020000.00: base + 10 % + 10 %.
- Una consulta posterior al COMMIT confirmó la reserva, relaciones, fechas, precio y coincidencia entre sucursal del paquete y vendedor. Es una carga directa de datos: no demuestra creación por HTTP ni ejecución de las validaciones de los services. No se modificaron schema.sql ni código de aplicación. Estos IDs quedan disponibles para practicar en Postman; la verificación HTTP sigue pendiente.

## Claves de Prueba y Consulta Manual — 25/10/2026
- El usuario confirmó que funcionó la consulta de la reserva de prueba después de indicar el GET con el token del Cliente. Esto no confirma todavía el rechazo de reservas ajenas ni el resto de la matriz de permisos.
- Pruebas HTTP confirmadas por el usuario para GET /api/paquetes-reservas/1: vendedor_b1 (sucursal 3) recibe 404 por pertenecer el paquete a la sucursal 2; vendedor_a1 (sucursal 2) recibe 200 y puede consultar la reserva. Queda confirmado ese par de casos de acceso por sucursal, sin extender la conclusión a escritura, listado u otros roles no probados.
- A pedido del usuario se restablecieron las contraseñas de todas las cuentas existentes excepto las de rol Administrador: prueba_duenio, vendedor_a1, vendedor_a2, vendedor_b1 y juan_viajes. La nueva clave acordada no se registra en este archivo.
- Se guardaron hashes BCrypt individuales y se incrementó version_autenticacion en uno para esas cinco cuentas, invalidando sus JWT anteriores. Se verificó con BCrypt que todas aceptan la clave nueva y que prueba_admin conserva exactamente su hash y versión anteriores. Carga directa en MySQL mediante una transacción; no se modificó código de aplicación ni schema.sql. Se requiere nuevo login para las cuentas modificadas.

## Tareas Pendientes (Backlog)
- [x] Agregar filtro opcional por nombre a los listados de paquetes, ciudades y hoteles con lógica compartida.
- [x] Implementar filtros combinados de hoteles por nombre/ciudad y paquetes por nombre/ciudad/hotel, con ejemplos confirmados en Postman.
- [ ] Adaptar FiltroNombreControllerTest a buscar(...) y agregar cobertura de filtros combinados, individuales y ausentes.
- [ ] Completar la matriz HTTP de filtros: ciudades, mayúsculas/minúsculas, espacios y todas las combinaciones no confirmadas todavía.
- [x] Permitir al Cliente listar y consultar por ID sus propias reservas, conservando los permisos de gestión.
- [ ] Confirmar por HTTP la consulta de reservas propias y rechazo de ajenas con Cliente, los permisos de otros roles y el bloqueo de escritura para Cliente.
- [x] Implementar GET /api/usuarios/me con cuenta autenticada y perfil de Turista para Cliente; pruebas unitarias aprobadas.
- [ ] Confirmar /api/usuarios/me por HTTP: Cliente con turista, gestión sin turista y sin token 401.

### Mi perfil — 25/10/2026
- MiPerfilResponse agrupa UsuarioResponse y TuristaResponse. UsuarioService.obtenerMiPerfil usa UsuarioActualService.obtener y, únicamente para Cliente, reutiliza TuristaService.obtener con el ID compartido. Devuelve turista null para gestión o si falta el perfil. No devuelve contraseña ni acepta ID del solicitante.
- UsuarioController devuelve ResponseEntity<MiPerfilResponse>. SecurityConfig permite GET exacto /api/usuarios/me con authenticated() antes de las reglas generales de usuarios.
- RegistroClienteServiceTest aprobó 42 casos (cinco nuevos de perfil: Cliente, tres roles de gestión y Cliente sin turista). Compilación aprobada. Falta prueba HTTP de la regla de seguridad; no se modificaron tablas ni schema.sql.
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
- [x] Definir e implementar logout global, con prueba manual de 204 y rechazo del token anterior con 401.
- [x] Probar logout sin token y nuevo login con la misma contraseña seguido de acceso con el nuevo JWT.
- [ ] Completar pruebas HTTP de logout con otros roles y otros tokens de la cuenta.
- [ ] Definir renovación de JWT y revisar CORS para el frontend.
- [x] Corregir la regla del listado de clientes para Vendedor a /api/usuarios/clientes.
- [ ] Verificar por HTTP el listado de clientes como Vendedor con sucursal y los rechazos a otros roles sin permiso.
- [x] Preparar email único en MySQL, validar duplicados en creación/edición de turistas y eliminar Cliente de prueba sin Turista.
- [x] Reutilizar TuristaRequest sin idUsuario, conservar la relación al editar y recibir el ID por ruta en la creación de gestión.
- [ ] Verificar por HTTP la edición de turistas conservando la cuenta relacionada.
- [x] Implementar registro público de Cliente y Turista juntos dentro de una transacción.
- [x] Crear RegistroClienteRequest con credenciales y TuristaRequest anidado validado.
- [x] Agregar el método transaccional de registro con rol Cliente fijo desde el servidor y pruebas unitarias.
- [x] Conectar el endpoint público de registro y permitir solamente su POST exacto.
- [x] Confirmar manualmente registro público y posterior login con nombre de usuario.
- [ ] Verificar los dos registros con ID compartido y el rollback por email duplicado en MySQL.
- [x] Implementar jerarquía de creación y asignación de roles: Dueño puede asignar todos; Administrador solo Vendedor/Cliente.
- [ ] Verificar por HTTP la creación conjunta desde gestión y la jerarquía al crear/editar/eliminar.
- [x] Implementar login unificado Cliente por username/email, con pruebas automatizadas y contexto Spring/MySQL aprobado.
- [ ] Confirmar por HTTP login Cliente con username/email, rechazos y acceso autenticado usando el JWT emitido por email.
- [x] Unificar AuthRequest con identificador y contraseña y eliminar el DTO de login separado.
- [x] Agregar validaciones cruzadas de username/email en registro, gestión y edición con lógica compartida y pruebas.
- [ ] Confirmar por HTTP los rechazos de coincidencias cruzadas y la conservación de valores propios.
- [ ] Implementar ranking de vendedores según sus ventas para el Dueño.

## Problemas o Bugs Conocidos
- Completar las pruebas manuales y definir permisos adicionales del Cliente antes de considerar completa la seguridad.
- Revisar el manejo de errores si un 404/400 esperado termina en 401 al pasar por el despacho interno de errores. Ya se observó un 401 durante una creación de sucursal con datos inválidos; causa del despacho no confirmada con una prueba de integración. SecurityConfig no tiene una regla explícita para DispatcherType.ERROR. No dar por probado el 404 HTTP de ResponseStatusException solo por revisar el código.
- El matcher del listado de cuentas Cliente ya coincide con el controlador. Su verificación HTTP con Vendedor sigue pendiente; no presentar el antiguo desajuste de rutas como un problema todavía vigente.

## Continuación en Otro Chat
1. Leer Agents.md y este Memory.md, y revisar el código actual antes de proponer cambios.
   - Último avance: filtros combinados de hoteles y paquetes implementados por el usuario y probados manualmente en los ejemplos registrados. Próximo paso recomendado: adaptar las pruebas automáticas a buscar(...) antes de extender los filtros. No volver a implementar las consultas ni presentar el éxito histórico de FiltroNombreControllerTest como verificación de los cambios actuales.
2. Probar en Postman las validaciones cruzadas ya implementadas: username igual a email ajeno y email igual a username ajeno deben generar 400 sin crear la cuenta. Repetir al editar, permitiendo conservar valores propios. Luego probar login unificado del Cliente por username/email, contraseña incorrecta y JWT emitido por email. No repetir código: AuthRequest usa identificador/contraseña, provider usa loadUserByIdentificador y el filtro loadUserByUsername canónico. Las pruebas de login, validador, registro y turistas ya pasaron; el contexto también inicia con MySQL. Mostrar siempre bloques modificados y mantener pasos pequeños. Siguen pendientes ID compartido, rollback real ante fallo posterior al INSERT, gestión y jerarquía por HTTP.
3. Completar las pruebas manuales de DELETE /api/paquetes-reservas/{id}. Usar reservas de prueba de dos sucursales, confirmar estados y ausencia de borrado al rechazar. Si aparece 401 en vez de 404, investigar el despacho de errores.
4. Confirmar en Postman creación, actualización y precios: 0/10/20 % sobre el precio base; al editar una reserva de un compañero conservar vendedorId; impedir cambio a un paquete de otra sucursal.
5. Verificar token vencido y token de usuario eliminado; probar el listado de clientes como Vendedor (ruta ya corregida), completar pruebas de logout con otros roles y dos tokens, definir permisos adicionales del Cliente, revisar rutas sin regla específica (actualmente anyRequest().authenticated()), y decidir renovación y CORS cuando corresponda.
6. Retomar el ranking del Dueño después: existe consulta por monto vendido, falta endpoint/DTO y pruebas. Revisar también que los turistas estén vinculados a usuarios con rol Cliente.

### Preferencias de Enseñanza
- Explicar en español argentino, desde cero, lento y por pasos pequeños, con emojis y ejemplos concretos.
- El usuario prefiere implementar los ejercicios y pedir revisión. Modificar su código cuando lo pida explícitamente; en este chat pidió ayuda directa para separar los métodos y proteger actualización.
- Hacer preguntas de comprensión cuando sean útiles, sin cantidad fija. No avanzar varios temas juntos.
- Preferencia del usuario del 13/10: cada vez que se modifique código, mostrar el archivo y el bloque modificado (antes/después cuando ayude), explicar qué hace y por qué, e indicar cómo se verificó y qué queda pendiente. No limitarse a decir que el cambio está hecho.
- Se explicaron BigDecimal, factor inicial 1 (=100 %), adicional 0.10 y setScale(2, HALF_UP). Ejemplo correcto: 25.678 se redondea a 25.68.
- Usa NetBeans en Windows 10 y Postman; no registrar JWT_SECRET, tokens ni credenciales nuevas en esta memoria.

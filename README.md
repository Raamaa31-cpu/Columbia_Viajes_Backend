## 🔐 Credenciales por Defecto (Entorno de Desarrollo)

Al iniciar la aplicación por primera vez (`./mvnw spring-boot:run`), la clase `DataInitializer` verifica y crea automáticamente el usuario administrador con el rol correspondiente si la base de datos está vacía.

* **Usuario:** `admin`
* **Contraseña:** `root`
* **Rol ID:** `1` (`ADMIN`)

### Ejemplo de Petición de Prueba (Login)

Puedes probar la autenticación enviando una solicitud HTTP POST al endpoint de login:

```bash
curl -i -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"nombre":"admin","contrasenia":"root"}'

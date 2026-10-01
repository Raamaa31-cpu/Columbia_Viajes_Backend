package com.columbia.viajes.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO para recibir las credenciales en el endpoint de login.
 *
 * <p>No es un DTO de dominio; es un DTO de <b>autenticación</b>.
 * Sólo contiene el nombre de usuario y la contraseña sin encriptar.
 * La encriptación se compara luego mediante {@link org.springframework.security.crypto.password.PasswordEncoder}.</p>
 */
public record AuthRequest(

    @NotBlank(message = "El nombre de usuario no puede estar vacío")
    @Size(min = 2, max = 100, message = "El nombre de usuario debe tener entre 2 y 100 caracteres")
    String nombre,

    @NotBlank(message = "La contraseña no puede estar vacía")
    @Size(min = 8, max = 64, message = "La contraseña debe tener entre 8 y 64 caracteres")
    String contrasenia
) {}

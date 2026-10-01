package com.columbia.viajes.security;

import com.columbia.viajes.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Configuración central de Spring Security.
 *
 * <p>Explicación paso a paso de qué hace cada cosa:</p>
 * <ul>
 *   <li>{@code @EnableWebSecurity}: le dice a Spring que esta clase gestiona la
 *       configuración de seguridad web (reemplaza a la configuración automática
 *       por defecto).</li>
 *   <li>{@code @EnableMethodSecurity}: habilita anotaciones como
 *       {@code @PreAuthorize} y {@code @Secured} para proteger métodos
 *       individuales del service layer.</li>
 *   <li>{@code SecurityFilterChain}: bean que define <b>cómo</b> Spring Security
 *       filtra las peticiones HTTP. Es el bean más importante.</li>
 *   <li>{@code csrf.disable()}: desactiva protección CSRF porque esta es una REST API
 *       (no un formulario HTML tradicional; CSRF se basa en cookies y formularios).</li>
 *   <li>{@code SessionCreationPolicy.STATELESS}: le dice a Spring que <b>no
 *       use sesiones HTTP</b>. Cada request lleva su token JWT y se valida
 *       independentemente. Ideal para APIs REST escalables.</li>
 *   <li>{@code permitAll()} en {@code /api/auth/**}: el login y el registro
 *       deben ser endpoints públicos — si no, nadie podría autenticarse.</li>
 *   <li>{@code authenticated()} en el resto: cualquier otra ruta requiere
 *       estar autenticado (enviar un JWT válido).</li>
 *   <li>{@code JwtAuthenticationFilter}: filtro personalizado que intercepta
 *       cada request, extrae el token JWT del header {@code Authorization},
 *       lo valida y, si es válido, setea el usuario en el SecurityContext
 *       antes de que llegue al controlador.</li>
 *   <li>{@code PasswordEncoder}: bean que encripta contraseñas con BCrypt
 *       (algoritmo estándar, con salt incorporado).</li>
 *   <li>{@code AuthenticationManager}: bean que orquesta el proceso de
 *       autenticación (Spring lo necesita para comparar username/password).</li>
 * </ul>
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        // BCrypt con strength 12 (12 rounds de hashing). Es el estándar actual
        // para encriptación de contraseñas. Cada hash incluye un "salt" único.
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
}

package com.columbia.viajes.security;

import com.columbia.viajes.security.service.JwtService;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;


@Configuration
public class SecurityConfig {

    @Bean
    public UrlBasedCorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:8081"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);
        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtService jwtService, UserDetailsService userDetailsService) throws Exception {
        
        var filtroJwt = new JwtAuthenticationFilter(jwtService, userDetailsService);
        
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())
                .logout(logout -> logout.disable())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .exceptionHandling(errores -> errores
                        .authenticationEntryPoint((request, response, exception) -> {
                            response.setStatus(401);
                            response.setContentType("text/plain;charset=UTF-8");
                            response.getWriter().write("Necesitás iniciar sesión");
                        })
                        .accessDeniedHandler((request, response, exception) -> {
                            response.setStatus(403);
                            response.setContentType("text/plain;charset=UTF-8");
                            response.getWriter().write(
                                    "No tenés permiso para realizar esta operación."
                            );
                        })
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.GET, "/api/paquetes-reservas/ranking-vendedores")
                        .hasAuthority("Dueño")
                        .requestMatchers(HttpMethod.GET, "/api/usuarios/me")
                        .authenticated()
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/auth/login",
                                "/api/auth/registro"
                        ).permitAll()
                        
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/paquetes",
                                "/api/paquetes/**",
                                "/api/ciudades",
                                "/api/ciudades/**",
                                "/api/hoteles",
                                "/api/hoteles/**",
                                "/api/vuelos",
                                "/api/vuelos/**",
                                "/api/sucursales",
                                "/api/sucursales/**"
                        ).permitAll()
                        
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/usuarios/vendedores",
                                "/api/usuarios/vendedores/**",
                                "/api/usuarios/clientes",
                                "/api/usuarios/clientes/**"
                        ).hasAnyAuthority("Vendedor", "Administrador", "Dueño")
                        
                       .requestMatchers(
                                HttpMethod.GET,
                                "/api/turistas",
                                "/api/turistas/**"
                        ).hasAnyAuthority("Vendedor", "Administrador", "Dueño")
                        
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/paquetes-reservas",
                                "/api/paquetes-reservas/{id}"
                        ).hasAnyAuthority("Cliente", "Vendedor", "Administrador", "Dueño")

                        .requestMatchers(
                                "/api/paquetes-reservas",
                                "/api/paquetes-reservas/**"
                        ).hasAnyAuthority("Vendedor", "Administrador", "Dueño")
                        
                        .requestMatchers(
                                "/api/usuarios", 
                                "/api/usuarios/**",
                                "/api/paquetes",
                                "/api/paquetes/**",
                                "/api/ciudades",
                                "/api/ciudades/**",
                                "/api/hoteles",
                                "/api/hoteles/**",
                                "/api/vuelos",
                                "/api/vuelos/**",
                                "/api/sucursales",
                                "/api/sucursales/**",
                                "/api/turistas",
                                "/api/turistas/**",
                                "/api/roles",
                                "/api/roles/**"
                        )                     
                        .hasAnyAuthority("Administrador", "Dueño")
                        
                        .anyRequest().authenticated()
                );
        
        http.addFilterBefore(filtroJwt, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
        
    }
    
}

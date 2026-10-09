package com.columbia.viajes.config;

import com.columbia.viajes.model.Rol;
import com.columbia.viajes.model.Usuario;
import com.columbia.viajes.repository.RolRepository;
import com.columbia.viajes.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        Rol rolAdmin = rolRepository.findById(1).orElseGet(() -> {
            Rol nuevoRol = new Rol();
            nuevoRol.setNombre("ADMIN");
            return rolRepository.save(nuevoRol);
        });

        if (usuarioRepository.findByNombre("admin").isEmpty()) {
            Usuario admin = new Usuario();
            admin.setNombre("admin");
            admin.setContrasenia(passwordEncoder.encode("root"));
            admin.setRol(rolAdmin);
            admin.setFechaCreacion(LocalDateTime.now());

            usuarioRepository.save(admin);
            System.out.println(">>> [INIT] Usuario 'admin' creado exitosamente con contraseña 'root'");
        }
    }
}
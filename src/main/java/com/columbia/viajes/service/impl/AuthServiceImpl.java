package com.columbia.viajes.service.impl;

import com.columbia.viajes.dto.request.LoginRequest;
import com.columbia.viajes.dto.response.LoginResponse;
import com.columbia.viajes.model.Usuario;
import com.columbia.viajes.repository.UsuarioRepository;
import com.columbia.viajes.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public AuthServiceImpl(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        Usuario usuario = usuarioRepository.findByNombre(request.getNombre())
                .orElseThrow(() -> new RuntimeException("Usuario o contraseña incorrectos"));

        if (!passwordEncoder.matches(request.getContrasenia(), usuario.getContrasenia())) {
            throw new RuntimeException("Usuario o contraseña incorrectos");
        }

        Integer idRol = (usuario.getRol() != null) ? usuario.getRol().getId() : null;

        return new LoginResponse("Login exitoso", usuario.getNombre(), idRol, null);
    }
}
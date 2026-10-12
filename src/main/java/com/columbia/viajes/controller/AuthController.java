package com.columbia.viajes.controller;

import com.columbia.viajes.dto.request.AuthRequest;
import com.columbia.viajes.dto.request.CambioContraseniaRequest;
import com.columbia.viajes.dto.response.AuthResponse;
import com.columbia.viajes.service.AuthService;
import com.columbia.viajes.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    
    private final UsuarioService usuarioService;
    
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request){
        return ResponseEntity.ok(authService.autenticar(request));
    }
    
    @PostMapping("/cambiar-contrasenia")
    public ResponseEntity<Void> cambiarContrasenia(@Valid @RequestBody CambioContraseniaRequest request){
        usuarioService.cambiarContrasenia(request);
        
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> cerrarTodasLasSesiones() {
        usuarioService.cerrarTodasLasSesiones();
        return ResponseEntity.noContent().build();
    }
    
}

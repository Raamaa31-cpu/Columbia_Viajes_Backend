package com.columbia.viajes.service;

import com.columbia.viajes.repository.TuristaRepository;
import com.columbia.viajes.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ValidadorIdentificadoresService {

    private final UsuarioRepository usuarioRepository;
    private final TuristaRepository turistaRepository;

    public void validarNombreDisponible(String nombre, Integer idUsuario) {
        usuarioRepository.findByNombre(nombre)
                .filter(usuario -> perteneceAOtraCuenta(usuario.getId(), idUsuario))
                .ifPresent(usuario -> {
                    throw new IllegalArgumentException("Ya existe un usuario registrado con el nombre: " + nombre);
                });

        turistaRepository.findByEmailIgnoreCase(nombre)
                .filter(turista -> perteneceAOtraCuenta(turista.getId(), idUsuario))
                .ifPresent(turista -> {
                    throw new IllegalArgumentException("El nombre de usuario coincide con un email registrado por otra cuenta.");
                });
    }

    public void validarEmailDisponible(String email, Integer idUsuario) {
        turistaRepository.findByEmailIgnoreCase(email)
                .filter(turista -> perteneceAOtraCuenta(turista.getId(), idUsuario))
                .ifPresent(turista -> {
                    throw new IllegalArgumentException("Ya existe un turista registrado con el email: " + email);
                });

        usuarioRepository.findByNombre(email)
                .filter(usuario -> perteneceAOtraCuenta(usuario.getId(), idUsuario))
                .ifPresent(usuario -> {
                    throw new IllegalArgumentException("El email coincide con un nombre de usuario registrado por otra cuenta.");
                });
    }

    private boolean perteneceAOtraCuenta(Integer idEncontrado, Integer idUsuario) {
        return idUsuario == null || !idUsuario.equals(idEncontrado);
    }
}

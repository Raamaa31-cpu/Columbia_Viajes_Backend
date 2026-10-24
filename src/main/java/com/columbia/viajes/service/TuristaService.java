package com.columbia.viajes.service;

import com.columbia.viajes.dto.request.TuristaRequest;
import com.columbia.viajes.dto.response.TuristaResponse;
import com.columbia.viajes.mapper.TuristaMapper;
import com.columbia.viajes.model.Turista;
import com.columbia.viajes.model.Usuario;
import com.columbia.viajes.repository.TuristaRepository;
import com.columbia.viajes.repository.UsuarioRepository;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TuristaService {

    private final TuristaRepository turistaRepository;
    private final UsuarioRepository usuarioRepository;
    private final TuristaMapper turistaMapper;

    public List<TuristaResponse> listar() {
        return turistaRepository.findAll()
                .stream()
                .map(turistaMapper::toTuristaResponse)
                .toList();
    }

    public Optional<TuristaResponse> obtener(Integer id) {
        return turistaRepository.findById(id)
                .map(turistaMapper::toTuristaResponse);
    }

    @Transactional
    public TuristaResponse crear(Integer idUsuario, TuristaRequest request) {
        validarEmailDisponible(request.email(), null);

        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new IllegalArgumentException("El usuario con ID " + idUsuario + " no existe."));

        if (!"Cliente".equals(usuario.getRol().getNombre())) {
            throw new IllegalArgumentException("Solo los usuarios con rol Cliente pueden tener un turista asociado.");
        }
        if (turistaRepository.existsById(idUsuario)) {
            throw new IllegalArgumentException("El usuario con ID " + idUsuario + " ya tiene un turista asociado.");
        }

        Turista turistaNuevo = turistaMapper.toTurista(request, usuario);
        Turista turistaGuardado = turistaRepository.save(turistaNuevo);
        return turistaMapper.toTuristaResponse(turistaGuardado);
    }

    @Transactional
    public Optional<TuristaResponse> actualizar(Integer id, TuristaRequest request) {
        return turistaRepository.findById(id)
                .map(turista -> {
                    validarEmailDisponible(request.email(), turista.getId());

                    turistaMapper.actualizarTurista(request, turista);

                    Turista turistaActualizado = turistaRepository.save(turista);
                    return turistaMapper.toTuristaResponse(turistaActualizado);
                });
    }

    @Transactional
    public void eliminar(Integer id) {
        Turista turista = turistaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró el turista con ID " + id));

        turistaRepository.delete(turista);
    }

    private void validarEmailDisponible(String email, Integer idTurista) {
        turistaRepository.findByEmailIgnoreCase(email)
                .filter(turista -> !turista.getId().equals(idTurista))
                .ifPresent(turista -> {
                    throw new IllegalArgumentException("Ya existe un turista registrado con el email: " + email);
                });
    }
}

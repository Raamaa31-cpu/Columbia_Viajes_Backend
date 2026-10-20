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
    public TuristaResponse crear(TuristaRequest request) {
        // El email no es único en la tabla turistas (ver schema.sql), por lo que
        // no se valida unicidad. Se valida unicidad solo donde la base de datos lo define.

        Usuario usuario = resolverUsuario(request.idUsuario(), null);

        Turista turistaNuevo = turistaMapper.toTurista(request, usuario);
        Turista turistaGuardado = turistaRepository.save(turistaNuevo);
        return turistaMapper.toTuristaResponse(turistaGuardado);
    }

    @Transactional
    public Optional<TuristaResponse> actualizar(Integer id, TuristaRequest request) {
        return turistaRepository.findById(id)
                .map(turista -> {
                    // El email no es único en la tabla turistas (ver schema.sql)

                    Usuario usuario = resolverUsuario(request.idUsuario(), id);

                    turistaMapper.actualizarTurista(request, usuario, turista);

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

    /**
     * La cuenta del turista es opcional. Si se indica, debe ser un usuario con rol
     * Cliente que no esté asociado a otro turista.
     * idTuristaActual: null al crear; el id del turista al actualizar.
     */
    private Usuario resolverUsuario(Integer idUsuario, Integer idTuristaActual) {
        if (idUsuario == null) {
            return null;
        }

        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new IllegalArgumentException("El usuario con ID " + idUsuario + " no existe."));

        if (!"Cliente".equals(usuario.getRol().getNombre())) {
            throw new IllegalArgumentException("Solo un usuario con rol Cliente puede asociarse a un turista.");
        }

        boolean yaAsociado = idTuristaActual == null
                ? turistaRepository.existsByUsuario_Id(idUsuario)
                : turistaRepository.existsByUsuario_IdAndIdNot(idUsuario, idTuristaActual);
        if (yaAsociado) {
            throw new IllegalArgumentException("El usuario con ID " + idUsuario + " ya está asociado a otro turista.");
        }

        return usuario;
    }
}

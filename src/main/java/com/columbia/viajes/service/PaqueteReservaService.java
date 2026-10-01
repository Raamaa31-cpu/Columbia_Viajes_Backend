package com.columbia.viajes.service;

import com.columbia.viajes.dto.request.PaqueteReservaRequest;
import com.columbia.viajes.dto.response.PaqueteReservaResponse;
import com.columbia.viajes.mapper.PaqueteReservaMapper;
import com.columbia.viajes.model.Paquete;
import com.columbia.viajes.model.PaqueteReserva;
import com.columbia.viajes.model.Turista;
import com.columbia.viajes.model.Usuario;
import com.columbia.viajes.repository.PaqueteRepository;
import com.columbia.viajes.repository.PaqueteReservaRepository;
import com.columbia.viajes.repository.TuristaRepository;
import com.columbia.viajes.repository.UsuarioRepository;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaqueteReservaService {

    private final PaqueteReservaRepository paqueteReservaRepository;
    private final PaqueteRepository paqueteRepository;
    private final TuristaRepository turistaRepository;
    private final UsuarioRepository usuarioRepository;
    private final PaqueteReservaMapper paqueteReservaMapper;

    public List<PaqueteReservaResponse> listar() {
        return paqueteReservaRepository.findAll()
                .stream()
                .map(paqueteReservaMapper::toPaqueteReservaResponse)
                .toList();
    }

    public Optional<PaqueteReservaResponse> obtener(Integer id) {
        return paqueteReservaRepository.findById(id)
                .map(paqueteReservaMapper::toPaqueteReservaResponse);
    }

    @Transactional
    public PaqueteReservaResponse crear(PaqueteReservaRequest request) {
        Paquete paquete = paqueteRepository.findById(request.idPaquete())
                .orElseThrow(() -> new IllegalArgumentException("El paquete con ID " + request.idPaquete() + " no existe."));

        Turista turista = turistaRepository.findById(request.idTurista())
                .orElseThrow(() -> new IllegalArgumentException("El turista con ID " + request.idTurista() + " no existe."));

        Usuario vendedor = usuarioRepository.findById(request.idVendedor())
                .orElseThrow(() -> new IllegalArgumentException("El usuario (vendedor) con ID " + request.idVendedor() + " no existe."));

        PaqueteReserva reservaNueva = paqueteReservaMapper.toPaqueteReserva(request, paquete, turista, vendedor);
        PaqueteReserva reservaGuardada = paqueteReservaRepository.save(reservaNueva);
        return paqueteReservaMapper.toPaqueteReservaResponse(reservaGuardada);
    }

    @Transactional
    public Optional<PaqueteReservaResponse> actualizar(Integer id, PaqueteReservaRequest request) {
        return paqueteReservaRepository.findById(id)
                .map(reserva -> {
                    Paquete paquete = paqueteRepository.findById(request.idPaquete())
                            .orElseThrow(() -> new IllegalArgumentException("El paquete con ID " + request.idPaquete() + " no existe."));

                    Turista turista = turistaRepository.findById(request.idTurista())
                            .orElseThrow(() -> new IllegalArgumentException("El turista con ID " + request.idTurista() + " no existe."));

                    Usuario vendedor = usuarioRepository.findById(request.idVendedor())
                            .orElseThrow(() -> new IllegalArgumentException("El usuario (vendedor) con ID " + request.idVendedor() + " no existe."));

                    paqueteReservaMapper.actualizarPaqueteReserva(request, paquete, turista, vendedor, reserva);

                    PaqueteReserva reservaActualizada = paqueteReservaRepository.save(reserva);
                    return paqueteReservaMapper.toPaqueteReservaResponse(reservaActualizada);
                });
    }

    @Transactional
    public void eliminar(Integer id) {
        PaqueteReserva reserva = paqueteReservaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró la reserva con ID " + id));

        paqueteReservaRepository.delete(reserva);
    }
}

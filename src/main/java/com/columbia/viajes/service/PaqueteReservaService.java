package com.columbia.viajes.service;

import com.columbia.viajes.dto.request.PaqueteReservaRequest;
import com.columbia.viajes.dto.response.PaqueteReservaResponse;
import com.columbia.viajes.mapper.PaqueteReservaMapper;
import com.columbia.viajes.model.Paquete;
import com.columbia.viajes.model.ClaseVuelo;
import com.columbia.viajes.model.RegimenHospedaje;
import com.columbia.viajes.model.PaqueteReserva;
import com.columbia.viajes.model.Turista;
import com.columbia.viajes.model.Usuario;
import com.columbia.viajes.repository.PaqueteRepository;
import com.columbia.viajes.repository.PaqueteReservaRepository;
import com.columbia.viajes.repository.TuristaRepository;
import com.columbia.viajes.repository.UsuarioRepository;
import com.columbia.viajes.security.service.UsuarioActualService;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class PaqueteReservaService {

    private final PaqueteReservaRepository paqueteReservaRepository;
    private final PaqueteRepository paqueteRepository;
    private final TuristaRepository turistaRepository;
    private final UsuarioRepository usuarioRepository;
    
    private final PaqueteReservaMapper paqueteReservaMapper;

    private final UsuarioActualService usuarioActualService;
    
    public List<PaqueteReservaResponse> listar() {
        
        Usuario usuarioActual = usuarioActualService.obtener();
        String rol = usuarioActual.getRol().getNombre();
        List<PaqueteReserva> paquetesReservas;
        
        if(rol.equals("Administrador") || rol.equals("Dueño")){
            paquetesReservas = paqueteReservaRepository.findAll();
        } else if(rol.equals("Vendedor") && usuarioActual.getSucursal() != null){
            paquetesReservas = paqueteReservaRepository.findByPaquete_Sucursal_Id(usuarioActual.getSucursal().getId());
        } else if(rol.equals("Cliente")){
            paquetesReservas = paqueteReservaRepository.findByTurista_Id(usuarioActual.getId());
        } else {
            throw new AccessDeniedException("Acceso denegado");
        }
        
        return paquetesReservas
                .stream()
                .map(paqueteReservaMapper::toPaqueteReservaResponse)
                .toList();
    }

    public Optional<PaqueteReservaResponse> obtener(Integer id) {
        Usuario usuarioActual = usuarioActualService.obtener();
        Optional<PaqueteReserva> reserva;

        if (usuarioActual.getRol().getNombre().equals("Cliente")) {
            reserva = paqueteReservaRepository.findByIdAndTurista_Id(id, usuarioActual.getId());
        } else {
            reserva = buscarReservaAccesible(id, usuarioActual);
        }

        return reserva.map(paqueteReservaMapper::toPaqueteReservaResponse);
    }

    @Transactional
    public PaqueteReservaResponse crear(PaqueteReservaRequest request) {
        
        Paquete paquete = paqueteRepository.findById(request.idPaquete())
                .orElseThrow(() -> new IllegalArgumentException("El paquete con ID " + request.idPaquete() + " no existe."));

        Turista turista = turistaRepository.findById(request.idTurista())
                .orElseThrow(() -> new IllegalArgumentException("El turista con ID " + request.idTurista() + " no existe."));

        Usuario usuarioActual = usuarioActualService.obtener();

        Usuario vendedor = resolverVendedorCreacion(request.idVendedor(), usuarioActual);
        
        validarVendedorDelPaquete(vendedor, paquete, usuarioActual.getRol().getNombre());
        
        PaqueteReserva reservaNueva = paqueteReservaMapper.toPaqueteReserva(request, paquete, turista, vendedor);
        reservaNueva.setPrecioReserva(calcularPrecioReserva(paquete, request));
        
        PaqueteReserva reservaGuardada = paqueteReservaRepository.save(reservaNueva);
        
        return paqueteReservaMapper.toPaqueteReservaResponse(reservaGuardada);
    }

    @Transactional
    public Optional<PaqueteReservaResponse> actualizar(Integer id, PaqueteReservaRequest request) {
        Usuario usuarioActual = usuarioActualService.obtener();
        String rolActual = usuarioActual.getRol().getNombre();

        return buscarReservaAccesible(id, usuarioActual)
                .map(reserva -> {
                    Paquete paquete = paqueteRepository.findById(request.idPaquete())
                            .orElseThrow(() -> new IllegalArgumentException("El paquete con ID " + request.idPaquete() + " no existe."));

                    Turista turista = turistaRepository.findById(request.idTurista())
                            .orElseThrow(() -> new IllegalArgumentException("El turista con ID " + request.idTurista() + " no existe."));

                    Usuario vendedor;
                    if (rolActual.equals("Vendedor")) {
                        vendedor = reserva.getVendedor();
                        if (!usuarioActual.getSucursal().getId().equals(paquete.getSucursal().getId())) {
                            throw new AccessDeniedException("Su sucursal no coincide con la del paquete seleccionado");
                        }
                    } else {
                        vendedor = resolverVendedorCreacion(request.idVendedor(), usuarioActual);
                    }

                    validarVendedorDelPaquete(vendedor, paquete, rolActual);

                    paqueteReservaMapper.actualizarPaqueteReserva(request, paquete, turista, vendedor, reserva);
                    reserva.setPrecioReserva(calcularPrecioReserva(paquete, request));

                    PaqueteReserva reservaActualizada = paqueteReservaRepository.save(reserva);
                    return paqueteReservaMapper.toPaqueteReservaResponse(reservaActualizada);
                });
    }

    @Transactional
    public void eliminar(Integer id) {
        Usuario usuarioActual = usuarioActualService.obtener();
        
        PaqueteReserva reserva = buscarReservaAccesible(id, usuarioActual)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No se encontro la reserva."
                ));

        paqueteReservaRepository.delete(reserva);
    }
    
    private BigDecimal calcularPrecioReserva(Paquete paquete, PaqueteReservaRequest request) {
        BigDecimal factor = BigDecimal.ONE;
        BigDecimal adicional = new BigDecimal("0.10");

        if (request.regimenHospedaje() == RegimenHospedaje.PENSION_COMPLETA) {
            factor = factor.add(adicional);
        }
        if (request.claseVuelo() == ClaseVuelo.PRIMERA) {
            factor = factor.add(adicional);
        }

        return paquete.getPrecio().multiply(factor).setScale(2, RoundingMode.HALF_UP);
    }

    private void validarVendedorDelPaquete(Usuario vendedor, Paquete paquete, String rolActual){
        if(!vendedor.getRol().getNombre().equals("Vendedor")){
            throw new IllegalArgumentException(
                "El ID seleccionado no pertenece a un vendedor"
            );
        }
        
        if (vendedor.getSucursal() == null) {
            throw new IllegalArgumentException(
                    "El vendedor seleccionado no tiene una sucursal asignada."
            );
        }        
        
        if(!vendedor.getSucursal().getId().equals(paquete.getSucursal().getId())){
            
            if(rolActual.equals("Vendedor")){
                throw new AccessDeniedException("Su sucursal no coincide con la del paquete seleccionado");
            } else {
                throw new IllegalArgumentException("La sucursal del vendedor no coincide con la del paquete seleccionado");
            }

        }        
    }
    
    private Optional<PaqueteReserva> buscarReservaAccesible(Integer id, Usuario usuarioActual) {
        String rol = usuarioActual.getRol().getNombre();
        if (rol.equals("Administrador") || rol.equals("Dueño")) {
            return paqueteReservaRepository.findById(id);
        }
        if (rol.equals("Vendedor") && usuarioActual.getSucursal() != null) {
            return paqueteReservaRepository.findByIdAndPaquete_Sucursal_Id(
                    id, usuarioActual.getSucursal().getId());
        }
        throw new AccessDeniedException("Acceso denegado");
    }

    private Usuario resolverVendedorCreacion(Integer id, Usuario usuarioActual){
        String rol = usuarioActual.getRol().getNombre();        
        
        Usuario vendedor;        
        
        if(rol.equals("Administrador") || rol.equals("Dueño")){

        if(id == null){
            throw new IllegalArgumentException(
                    "El ID del vendedor es obligatorio"
            );
        }

        vendedor = usuarioRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("El usuario (vendedor) con ID " + id + " no existe."));
            
        } else if(rol.equals("Vendedor") && usuarioActual.getSucursal() != null){
            
            vendedor = usuarioActual;

        } else {
            throw new AccessDeniedException("Acceso denegado");
        }
        
        return vendedor;
    }
}

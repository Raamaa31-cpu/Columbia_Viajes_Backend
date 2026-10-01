package com.columbia.viajes.service;

import com.columbia.viajes.dto.request.UsuarioRequest;
import com.columbia.viajes.dto.response.UsuarioResponse;
import com.columbia.viajes.mapper.UsuarioMapper;
import com.columbia.viajes.model.Rol;
import com.columbia.viajes.model.Sucursal;
import com.columbia.viajes.model.Usuario;
import com.columbia.viajes.repository.RolRepository;
import com.columbia.viajes.repository.SucursalRepository;
import com.columbia.viajes.repository.UsuarioRepository;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final SucursalRepository sucursalRepository;
    private final UsuarioMapper usuarioMapper;

    public List<UsuarioResponse> listar() {
        return usuarioRepository.findAll()
                .stream()
                .map(usuarioMapper::toUsuarioResponse)
                .toList();
    }

    public Optional<UsuarioResponse> obtener(Integer id) {
        return usuarioRepository.findById(id)
                .map(usuarioMapper::toUsuarioResponse);
    }

    @Transactional
    public UsuarioResponse crear(UsuarioRequest request) {
        if (usuarioRepository.findByNombre(request.nombre()).isPresent()) {
            throw new IllegalArgumentException("Ya existe un usuario registrado con el nombre: " + request.nombre());
        }

        Rol rol = rolRepository.findById(request.idRol())
                .orElseThrow(() -> new IllegalArgumentException("El rol con ID " + request.idRol() + " no existe."));

        Sucursal sucursal = obtenerSucursalVendedor(request, rol);

        Usuario usuarioNuevo = usuarioMapper.toUsuario(request, rol, sucursal);
        Usuario usuarioGuardado = usuarioRepository.save(usuarioNuevo);
        return usuarioMapper.toUsuarioResponse(usuarioGuardado);
    }

    @Transactional
    public Optional<UsuarioResponse> actualizar(Integer id, UsuarioRequest request) {
        return usuarioRepository.findById(id)
                .map(usuario -> {
                    if (!request.nombre().equals(usuario.getNombre()) 
                            && usuarioRepository.findByNombre(request.nombre()).isPresent()) {
                        throw new IllegalArgumentException("Ya existe un usuario registrado con el nombre: " + request.nombre());
                    }

                    Rol rol = rolRepository.findById(request.idRol())
                            .orElseThrow(() -> new IllegalArgumentException("El rol con ID " + request.idRol() + " no existe."));

                    Sucursal sucursal = obtenerSucursalVendedor(request, rol);

                    usuarioMapper.actualizarUsuario(request, rol, sucursal, usuario);

                    Usuario usuarioActualizado = usuarioRepository.save(usuario);
                    return usuarioMapper.toUsuarioResponse(usuarioActualizado);
                });
    }

    @Transactional
    public void eliminar(Integer id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró el usuario con ID " + id));

        usuarioRepository.delete(usuario);
    }
    
    private Sucursal obtenerSucursalVendedor(UsuarioRequest request, Rol rol){
        if (request.idSucursal() != null) {
            if (!"Vendedor".equalsIgnoreCase(rol.getNombre())) {
                throw new IllegalArgumentException("Solo los usuarios con rol Vendedor pueden tener una sucursal asignada.");
            }
            
            return sucursalRepository.findById(request.idSucursal())
                    .orElseThrow(() -> new IllegalArgumentException("La sucursal con ID " + request.idSucursal() + " no existe."));
        }
        
        return null;
    }
}
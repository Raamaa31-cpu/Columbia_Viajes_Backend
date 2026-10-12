package com.columbia.viajes.service;

import com.columbia.viajes.dto.request.CambioContraseniaRequest;
import com.columbia.viajes.dto.request.UsuarioActualizacionRequest;
import com.columbia.viajes.dto.request.UsuarioRequest;
import com.columbia.viajes.dto.response.UsuarioResponse;
import com.columbia.viajes.mapper.UsuarioMapper;
import com.columbia.viajes.model.Rol;
import com.columbia.viajes.model.Sucursal;
import com.columbia.viajes.model.Usuario;
import com.columbia.viajes.repository.RolRepository;
import com.columbia.viajes.repository.SucursalRepository;
import com.columbia.viajes.repository.UsuarioRepository;
import com.columbia.viajes.security.service.UsuarioActualService;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final SucursalRepository sucursalRepository;
    
    private final UsuarioMapper usuarioMapper;
    
    private final PasswordEncoder passwordEncoder;

    private final UsuarioActualService usuarioActualService;
    
    public List<UsuarioResponse> listar() {
        return usuarioRepository.findAll()
                .stream()
                .map(usuarioMapper::toUsuarioResponse)
                .toList();
    }

    public List<UsuarioResponse> listarVendedores(){
        
        Usuario usuarioActual = usuarioActualService.obtener();
        String rol = usuarioActual.getRol().getNombre();
        
        List<Usuario> vendedores;
        
        if(rol.equals("Administrador") || rol.equals("Dueño")){
            vendedores = usuarioRepository.findByRol_Nombre("Vendedor");
        } else if(rol.equals("Vendedor") && usuarioActual.getSucursal() != null){
            vendedores = usuarioRepository.findByRol_NombreAndSucursal_Id("Vendedor", usuarioActual.getSucursal().getId());
        } else {
            throw new AccessDeniedException("Acceso denegado");
        }
        
        return vendedores
                .stream()
                .map(usuarioMapper::toUsuarioResponse)
                .toList();
    }
    
    public List<UsuarioResponse> listarClientes(){
        Usuario usuarioActual = usuarioActualService.obtener();
        String rol = usuarioActual.getRol().getNombre();
        
        List<Usuario> clientes;
        
        if(rol.equals("Administrador") || rol.equals("Dueño") || (rol.equals("Vendedor") && usuarioActual.getSucursal() != null)){
            clientes = usuarioRepository.findByRol_Nombre("Cliente");
        }else {
            throw new AccessDeniedException("Acceso denegado");
        }
        
        return clientes
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

        Sucursal sucursal = obtenerSucursalVendedor(request.idSucursal(), rol);

        Usuario usuarioNuevo = usuarioMapper.toUsuario(request, rol, sucursal);
        
        usuarioNuevo.setContrasenia(
                passwordEncoder.encode(request.contrasenia())
        );
        
        Usuario usuarioGuardado = usuarioRepository.save(usuarioNuevo);
        return usuarioMapper.toUsuarioResponse(usuarioGuardado);
    }

    @Transactional
    public Optional<UsuarioResponse> actualizar(Integer id, UsuarioActualizacionRequest request) {
        return usuarioRepository.findById(id)
                .map(usuario -> {
                    if (!request.nombre().equals(usuario.getNombre()) 
                            && usuarioRepository.findByNombre(request.nombre()).isPresent()) {
                        throw new IllegalArgumentException("Ya existe un usuario registrado con el nombre: " + request.nombre());
                    }

                    Rol rol = rolRepository.findById(request.idRol())
                            .orElseThrow(() -> new IllegalArgumentException("El rol con ID " + request.idRol() + " no existe."));

                    Sucursal sucursal = obtenerSucursalVendedor(request.idSucursal(), rol);

                    usuarioMapper.actualizarUsuario(request, rol, sucursal, usuario);

                    Usuario usuarioActualizado = usuarioRepository.save(usuario);
                    return usuarioMapper.toUsuarioResponse(usuarioActualizado);
                });
    }
    
    @Transactional
    public void cambiarContrasenia(CambioContraseniaRequest request){
        Usuario usuarioActual = usuarioActualService.obtener();
        
        boolean coincide = passwordEncoder.matches(
                request.contraseniaActual(),
                usuarioActual.getContrasenia()
        );
        
        if(!coincide){
            throw new IllegalArgumentException("La contraseña actual es incorrecta");
        }
        
        String nuevoHash = passwordEncoder.encode(request.contraseniaNueva());
        usuarioActual.setContrasenia(nuevoHash);
        
        long versionAutenticacionNueva = usuarioActual.getVersionAutenticacion() + 1;
        usuarioActual.setVersionAutenticacion(versionAutenticacionNueva);
        
        usuarioRepository.save(usuarioActual);
    }

    @Transactional
    public void eliminar(Integer id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró el usuario con ID " + id));

        usuarioRepository.delete(usuario);
    }
    
    private Sucursal obtenerSucursalVendedor(Integer idSucursal, Rol rol){
        if (idSucursal != null) {
            if (!"Vendedor".equalsIgnoreCase(rol.getNombre())) {
                throw new IllegalArgumentException("Solo los usuarios con rol Vendedor pueden tener una sucursal asignada.");
            }
            
            return sucursalRepository.findById(idSucursal)
                    .orElseThrow(() -> new IllegalArgumentException("La sucursal con ID " + idSucursal + " no existe."));
        }
        
        return null;
    }
}
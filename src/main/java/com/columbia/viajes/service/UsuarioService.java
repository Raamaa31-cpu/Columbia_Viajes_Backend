package com.columbia.viajes.service;

import com.columbia.viajes.dto.request.CambioContraseniaRequest;
import com.columbia.viajes.dto.request.RegistroClienteRequest;
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
    private final TuristaService turistaService;
    private final ValidadorIdentificadoresService validadorIdentificadores;
    
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
        Rol rol = rolRepository.findById(request.idRol())
                .orElseThrow(() -> new IllegalArgumentException("El rol con ID " + request.idRol() + " no existe."));

        validarGestionDeRoles(null, rol);
        Sucursal sucursal = obtenerSucursalVendedor(request.idSucursal(), rol);

        Usuario usuarioGuardado = crearUsuario(request, rol, sucursal);
        return usuarioMapper.toUsuarioResponse(usuarioGuardado);
    }

    @Transactional
    public UsuarioResponse registrarCliente(RegistroClienteRequest request) {
        Rol rolCliente = rolRepository.findByNombre("Cliente")
                .orElseThrow(() -> new IllegalStateException("El rol Cliente no está configurado."));

        UsuarioRequest datosUsuario = new UsuarioRequest(
                request.nombreUsuario(), request.contrasenia(), rolCliente.getId(), null, request.turista()
        );

        Usuario usuarioGuardado = crearUsuario(datosUsuario, rolCliente, null);

        return usuarioMapper.toUsuarioResponse(usuarioGuardado);
    }

    @Transactional
    public Optional<UsuarioResponse> actualizar(Integer id, UsuarioActualizacionRequest request) {
        return usuarioRepository.findById(id)
                .map(usuario -> {
                    Rol rol = rolRepository.findById(request.idRol())
                            .orElseThrow(() -> new IllegalArgumentException("El rol con ID " + request.idRol() + " no existe."));

                    validarGestionDeRoles(usuario.getRol(), rol);
                    if ("Cliente".equals(usuario.getRol().getNombre()) != "Cliente".equals(rol.getNombre())) {
                        throw new IllegalArgumentException("No se puede cambiar entre Cliente y roles de gestión mediante esta operación.");
                    }
                    validadorIdentificadores.validarNombreDisponible(request.nombre(), usuario.getId());
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
        
        incrementarVersionAutenticacion(usuarioActual);
        
        usuarioRepository.save(usuarioActual);
    }

    @Transactional
    public void eliminar(Integer id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró el usuario con ID " + id));

        validarGestionDeRoles(usuario.getRol(), usuario.getRol());
        usuarioRepository.delete(usuario);
    }
    
    @Transactional
    public void cerrarTodasLasSesiones(){
        Usuario usuarioActual = usuarioActualService.obtener();
        
        incrementarVersionAutenticacion(usuarioActual);
        
        usuarioRepository.save(usuarioActual);
    }

    private void incrementarVersionAutenticacion(Usuario usuario) {
        long versionAutenticacionNueva = usuario.getVersionAutenticacion() + 1;
        usuario.setVersionAutenticacion(versionAutenticacionNueva);
    }

    private Usuario crearUsuario(UsuarioRequest request, Rol rol, Sucursal sucursal) {
        boolean esCliente = "Cliente".equals(rol.getNombre());
        if (esCliente && request.turista() == null) {
            throw new IllegalArgumentException("Los datos del turista son obligatorios para un Cliente.");
        }
        if (!esCliente && request.turista() != null) {
            throw new IllegalArgumentException("Solo los usuarios con rol Cliente pueden tener datos de turista.");
        }
        validadorIdentificadores.validarNombreDisponible(request.nombre(), null);
        if (esCliente) {
            validadorIdentificadores.validarEmailDisponible(request.turista().email(), null);
        }
        Usuario usuarioNuevo = usuarioMapper.toUsuario(request, rol, sucursal);
        usuarioNuevo.setContrasenia(passwordEncoder.encode(request.contrasenia()));
        Usuario usuarioGuardado = usuarioRepository.save(usuarioNuevo);
        if (esCliente) {
            turistaService.crear(usuarioGuardado.getId(), request.turista());
        }
        return usuarioGuardado;
    }

    private void validarGestionDeRoles(Rol rolActualObjetivo, Rol rolNuevo) {
        String rolCreador = usuarioActualService.obtener().getRol().getNombre();
        if ("Dueño".equals(rolCreador)) {
            return;
        }
        if ("Administrador".equals(rolCreador)
                && esRolGestionablePorAdministrador(rolNuevo)
                && (rolActualObjetivo == null || esRolGestionablePorAdministrador(rolActualObjetivo))) {
            return;
        }
        throw new AccessDeniedException("No tenés permiso para gestionar usuarios con ese rol.");
    }

    private boolean esRolGestionablePorAdministrador(Rol rol) {
        return "Cliente".equals(rol.getNombre()) || "Vendedor".equals(rol.getNombre());
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

package com.columbia.viajes.repository;

import com.columbia.viajes.model.Usuario;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {
    Optional<Usuario> findByNombre(String nombre);
    
    List<Usuario> findByRol_NombreAndSucursal_Id(String nombreRol, Integer idSucursal);
    
    List<Usuario> findByRol_Nombre(String nombreRol);
}

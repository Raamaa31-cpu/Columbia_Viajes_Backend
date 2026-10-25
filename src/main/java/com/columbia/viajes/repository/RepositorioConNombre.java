package com.columbia.viajes.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.util.StringUtils;

@NoRepositoryBean
public interface RepositorioConNombre<T> extends JpaRepository<T, Integer> {

    List<T> findByNombreContainingIgnoreCase(String nombre);

    default List<T> listarPorNombre(String nombre) {
        if (!StringUtils.hasText(nombre)) {
            return findAll();
        }
        return findByNombreContainingIgnoreCase(nombre.strip());
    }
}

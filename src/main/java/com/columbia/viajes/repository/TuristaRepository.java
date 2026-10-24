package com.columbia.viajes.repository;

import com.columbia.viajes.model.Turista;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TuristaRepository extends JpaRepository<Turista, Integer> {
    Optional<Turista> findByEmailIgnoreCase(String email);
}

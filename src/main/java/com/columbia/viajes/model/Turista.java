package com.columbia.viajes.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "turistas")
@Data
public class Turista {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    // Cuenta opcional: un turista puede existir sin usuario (rol Cliente)
    @OneToOne(optional = true)
    @JoinColumn(name = "id_usuario", unique = true)
    private Usuario usuario;
    
    private String nombre;
    private String apellidos;
    private String direccion;
    private String email;
    private String telefono;
}

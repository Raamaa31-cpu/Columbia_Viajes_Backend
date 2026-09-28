package com.columbia.viajes.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "turistas")
@Data
public class Turista {
    @Id
    private Integer id;
    
    @OneToOne(optional = false)
    @MapsId
    @JoinColumn(name = "id")
    private Usuario usuario;
    
    private String nombre;
    private String apellidos;
    private String direccion;
    private String email;
    private String telefono;
}

package com.columbia.viajes.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Data;

@Entity
@Table(name = "turistas", uniqueConstraints = {
    @UniqueConstraint(name = "uq_turistas_email", columnNames = "email")
})
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
    @Column(nullable = false, length = 120)
    private String email;
    private String telefono;
}

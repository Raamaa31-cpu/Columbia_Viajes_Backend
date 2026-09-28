package com.columbia.viajes.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "hoteles")
@Data
public class Hotel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    private String nombre;
    private String direccion;
    
    @ManyToOne(optional = false)
    @JoinColumn(name = "id_ciudad")
    private Ciudad ciudad;
    
    private String telefono;
    
    private int plazasTotales;
}
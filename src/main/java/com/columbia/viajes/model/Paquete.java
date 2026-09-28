package com.columbia.viajes.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "paquetes")
@Data
public class Paquete {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @ManyToOne(optional = false)
    @JoinColumn(name = "id_sucursal")
    private Sucursal sucursal;
    
    @ManyToOne(optional = false)
    @JoinColumn(name = "id_vuelo_ida")
    private Vuelo vueloIda;
    
    @ManyToOne(optional = false)
    @JoinColumn(name = "id_vuelo_vuelta")
    private Vuelo vueloVuelta;
    
    @ManyToOne(optional = false)
    @JoinColumn(name = "id_hotel")
    private Hotel hotel;
    
    private String nombre;
    
    @JdbcTypeCode(SqlTypes.LONGVARCHAR)
    private String descripcion;
    
    private LocalDate fechaLlegadaHotel;
    
    private LocalDate fechaPartidaHotel;
    
    private BigDecimal precio;
    
    private String imagen;
    
    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime fechaCreacion;
}

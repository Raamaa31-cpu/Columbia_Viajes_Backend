package com.columbia.viajes.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Data;

@Entity
@Table(name = "vuelos")
@Data
public class Vuelo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @ManyToOne(optional = false)
    @JoinColumn(name = "id_ciudad_origen")
    private Ciudad ciudadOrigen;
    
    @ManyToOne(optional = false)
    @JoinColumn(name = "id_ciudad_destino")
    private Ciudad ciudadDestino;
    
    private LocalDateTime fechaSalida;
    
    private LocalDateTime fechaLlegada;
    
    private Integer plazasTuristaTotales;
    
    private Integer plazasPrimeraTotales;
}

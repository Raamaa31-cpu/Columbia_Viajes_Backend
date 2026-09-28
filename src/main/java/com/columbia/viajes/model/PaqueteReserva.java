package com.columbia.viajes.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "paquetes_reservas")
@Data
public class PaqueteReserva {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @ManyToOne(optional = false)
    @JoinColumn(name = "id_paquete")
    private Paquete paquete;
    
    @ManyToOne(optional = false)
    @JoinColumn(name = "id_turista")
    private Turista turista;
    
    @Enumerated(EnumType.STRING)
    private ClaseVuelo claseVuelo;
    
    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime fechaReserva;
    
    @Enumerated(EnumType.STRING)
    private RegimenHospedaje regimenHospedaje;
    
    private BigDecimal precioReserva;
    
    @ManyToOne(optional = false)
    @JoinColumn(name = "id_vendedor")
    private Usuario vendedor;
}

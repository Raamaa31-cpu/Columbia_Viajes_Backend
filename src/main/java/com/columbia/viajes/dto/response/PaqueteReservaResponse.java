package com.columbia.viajes.dto.response;

import com.columbia.viajes.model.ClaseVuelo;
import com.columbia.viajes.model.RegimenHospedaje;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaqueteReservaResponse(
        Integer id,
        Integer paqueteId,
        Integer turistaId,
        ClaseVuelo claseVuelo,
        LocalDateTime fechaReserva,
        RegimenHospedaje regimenHospedaje,
        BigDecimal precioReserva,
        Integer vendedorId
) {}

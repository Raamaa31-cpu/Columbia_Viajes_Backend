package com.columbia.viajes.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PaqueteResponse(
        Integer id,
        String nombre,
        String descripcion,
        LocalDate fechaLlegadaHotel,
        LocalDate fechaPartidaHotel,
        BigDecimal precio,
        String imagen,
        Integer sucursalId,
        Integer vueloIdaId,
        Integer vueloVueltaId,
        Integer hotelId
) {}

package com.columbia.viajes.dto.response;

import java.time.LocalDateTime;

public record VueloResponse(
        Integer id,
        String ciudadOrigenNombre,
        String ciudadDestinoNombre,
        LocalDateTime fechaSalida,
        LocalDateTime fechaLlegada,
        Integer plazasTuristaTotales,
        Integer plazasPrimeraTotales
) {}

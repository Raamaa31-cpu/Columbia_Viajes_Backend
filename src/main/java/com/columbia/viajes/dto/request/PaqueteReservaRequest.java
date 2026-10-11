package com.columbia.viajes.dto.request;

import com.columbia.viajes.model.ClaseVuelo;
import com.columbia.viajes.model.RegimenHospedaje;
import jakarta.validation.constraints.NotNull;

public record PaqueteReservaRequest(

    @NotNull(message = "El ID del paquete es obligatorio")
    Integer idPaquete,

    @NotNull(message = "El ID del turista es obligatorio")
    Integer idTurista,

    @NotNull(message = "La clase de vuelo es obligatoria")
    ClaseVuelo claseVuelo,

    @NotNull(message = "El régimen de hospedaje es obligatorio")
    RegimenHospedaje regimenHospedaje,

    Integer idVendedor
) {}

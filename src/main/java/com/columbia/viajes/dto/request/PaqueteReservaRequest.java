package com.columbia.viajes.dto.request;

import com.columbia.viajes.model.ClaseVuelo;
import com.columbia.viajes.model.RegimenHospedaje;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record PaqueteReservaRequest(

    @NotNull(message = "El ID del paquete es obligatorio")
    Integer idPaquete,

    @NotNull(message = "El ID del turista es obligatorio")
    Integer idTurista,

    @NotNull(message = "La clase de vuelo es obligatoria")
    ClaseVuelo claseVuelo,

    @NotNull(message = "El régimen de hospedaje es obligatorio")
    RegimenHospedaje regimenHospedaje,

    @NotNull(message = "El precio de la reserva no puede estar vacío")
    @DecimalMin(value = "0.0", inclusive = true, message = "El precio no puede ser un valor negativo")
    BigDecimal precioReserva,

    @NotNull(message = "El ID del vendedor es obligatorio")
    Integer idVendedor
) {}

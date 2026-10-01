package com.columbia.viajes.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public record VueloRequest(
        @NotNull(message = "El ID de la ciudad de origen es obligatorio")
        Integer idCiudadOrigen,

        @NotNull(message = "El ID de la ciudad de destino es obligatorio")
        Integer idCiudadDestino,

        @NotNull(message = "La fecha de salida no puede estar vacía")
        @Future(message = "La fecha de salida debe ser una fecha futura")
        LocalDateTime fechaSalida,

        @NotNull(message = "La fecha de llegada no puede estar vacía")
        LocalDateTime fechaLlegada,

        @NotNull(message = "Las plazas totales de clase turista son obligatorias")
        @Min(value = 0, message = "Las plazas totales de clase turista no pueden ser un valor negativo")
        Integer plazasTuristaTotales,

        @NotNull(message = "Las plazas totales de primera clase son obligatorias")
        @Min(value = 0, message = "Las plazas totales de primera clase no pueden ser un valor negativo")
        Integer plazasPrimeraTotales
) {}

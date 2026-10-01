package com.columbia.viajes.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

public record PaqueteRequest(

    @NotBlank(message = "El nombre del paquete no puede estar vacío")
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    String nombre,

    @Size(max = 1000, message = "La descripción no puede exceder los 1000 caracteres")
    String descripcion,

    @NotNull(message = "La fecha de llegada al hotel es obligatoria")
    LocalDate fechaLlegadaHotel,

    @NotNull(message = "La fecha de partida del hotel es obligatoria")
    LocalDate fechaPartidaHotel,

    @NotNull(message = "El precio no puede estar vacío")
    @DecimalMin(value = "0.0", inclusive = true, message = "El precio no puede ser un valor negativo")
    BigDecimal precio,

    @Size(max = 255, message = "La URL de la imagen no puede exceder los 255 caracteres")
    String imagen,

    @NotNull(message = "El ID de la sucursal es obligatorio")
    Integer idSucursal,

    @NotNull(message = "El ID del vuelo de ida es obligatorio")
    Integer idVueloIda,

    @NotNull(message = "El ID del vuelo de vuelta es obligatorio")
    Integer idVueloVuelta,

    @NotNull(message = "El ID del hotel es obligatorio")
    Integer idHotel
) {}

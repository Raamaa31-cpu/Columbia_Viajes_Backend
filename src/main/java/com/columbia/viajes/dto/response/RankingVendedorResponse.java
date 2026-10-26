package com.columbia.viajes.dto.response;

import java.math.BigDecimal;

public record RankingVendedorResponse(
        Integer vendedorId,
        String nombre,
        BigDecimal totalVendido,
        Integer sucursalId
) {}

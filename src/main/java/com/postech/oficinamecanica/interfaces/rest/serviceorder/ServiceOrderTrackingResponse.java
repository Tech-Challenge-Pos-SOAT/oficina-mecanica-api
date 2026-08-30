package com.postech.oficinamecanica.interfaces.rest.serviceorder;

import java.math.BigDecimal;
import java.time.Instant;

/** Visao enxuta usada no acompanhamento pelo cliente. */
public record ServiceOrderTrackingResponse(
    Long id,
    String status,
    BigDecimal price,
    Instant createdAt,
    Instant updatedAt
) {}

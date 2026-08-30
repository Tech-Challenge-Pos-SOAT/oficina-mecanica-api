package com.postech.oficinamecanica.interfaces.rest.serviceorder;

import java.math.BigDecimal;
import java.time.Instant;

public record ServiceOrderHistoryResponse(
    String status,
    BigDecimal price,
    String authorType,
    Long authorId,
    String observation,
    Instant createdAt
) {}

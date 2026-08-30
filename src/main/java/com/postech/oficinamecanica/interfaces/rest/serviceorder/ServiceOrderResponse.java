package com.postech.oficinamecanica.interfaces.rest.serviceorder;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record ServiceOrderResponse(
    Long id,
    Long customerId,
    Long vehicleId,
    String status,
    BigDecimal price,
    List<ServiceOrderServiceResponse> services,
    List<ServiceOrderMaterialResponse> materials,
    List<ServiceOrderHistoryResponse> history,
    Instant createdAt,
    Instant updatedAt
) {}

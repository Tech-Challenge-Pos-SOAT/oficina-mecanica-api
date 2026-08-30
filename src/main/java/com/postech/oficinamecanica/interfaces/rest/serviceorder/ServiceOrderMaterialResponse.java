package com.postech.oficinamecanica.interfaces.rest.serviceorder;

import java.math.BigDecimal;

public record ServiceOrderMaterialResponse(
    Long id,
    Long materialId,
    Integer quantity,
    BigDecimal price,
    BigDecimal total
) {}

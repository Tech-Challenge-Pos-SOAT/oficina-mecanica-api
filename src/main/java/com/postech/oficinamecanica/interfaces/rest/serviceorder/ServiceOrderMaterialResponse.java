package com.postech.oficinamecanica.interfaces.rest.serviceorder;

import java.math.BigDecimal;

public record ServiceOrderMaterialResponse(
    Long id,
    Long materialId,
    Integer quantity,
    BigDecimal price,
    BigDecimal total,
    /** true = peca ja baixada do estoque, ou seja, veio de um ciclo aprovado. */
    boolean stockDebited
) {}

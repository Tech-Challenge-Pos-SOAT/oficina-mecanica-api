package com.postech.oficinamecanica.interfaces.rest.serviceorder;

import java.math.BigDecimal;

public record ServiceOrderServiceResponse(
    Long id,
    Long serviceId,
    BigDecimal price,
    /** true = servico ja autorizado pelo cliente em um ciclo anterior. */
    boolean approved
) {}

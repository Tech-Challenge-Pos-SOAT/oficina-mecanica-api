package com.postech.oficinamecanica.application.service;

import java.math.BigDecimal;

public record UpdateServiceCommand(
    String name,
    String description,
    BigDecimal price
) {}

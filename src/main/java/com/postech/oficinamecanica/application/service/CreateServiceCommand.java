package com.postech.oficinamecanica.application.service;

import java.math.BigDecimal;

public record CreateServiceCommand(
    String name,
    String description,
    BigDecimal price
) {}

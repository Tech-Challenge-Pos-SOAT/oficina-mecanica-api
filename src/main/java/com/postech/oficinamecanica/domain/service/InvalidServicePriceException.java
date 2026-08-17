package com.postech.oficinamecanica.domain.service;

import java.math.BigDecimal;

public class InvalidServicePriceException extends RuntimeException {
    public InvalidServicePriceException(BigDecimal price) {
        super("Invalid service price: " + price);
    }
}

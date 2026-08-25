package com.postech.oficinamecanica.domain.material;

import com.postech.oficinamecanica.domain.shared.exceptions.BusinessRuleViolationException;

public class InsufficientStockException extends BusinessRuleViolationException {
    public InsufficientStockException(String message) {
        super(message);
    }
}

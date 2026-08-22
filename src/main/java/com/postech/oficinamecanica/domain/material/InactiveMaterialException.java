package com.postech.oficinamecanica.domain.material;

import com.postech.oficinamecanica.domain.shared.exceptions.BusinessRuleViolationException;

public class InactiveMaterialException extends BusinessRuleViolationException {
    public InactiveMaterialException(String message) {
        super(message);
    }
}

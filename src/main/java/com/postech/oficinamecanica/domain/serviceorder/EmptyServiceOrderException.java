package com.postech.oficinamecanica.domain.serviceorder;

import com.postech.oficinamecanica.domain.shared.exceptions.BusinessRuleViolationException;

public class EmptyServiceOrderException extends BusinessRuleViolationException {
    public EmptyServiceOrderException(Long id) {
        super("Service order " + id + " has no services or materials to budget");
    }
}

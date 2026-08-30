package com.postech.oficinamecanica.domain.service;

import com.postech.oficinamecanica.domain.shared.exceptions.BusinessRuleViolationException;

public class InactiveServiceException extends BusinessRuleViolationException {
    public InactiveServiceException(Long id) {
        super("Cannot add inactive service " + id + " to a service order");
    }
}

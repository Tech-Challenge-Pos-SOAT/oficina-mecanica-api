package com.postech.oficinamecanica.domain.serviceorder;

public class ServiceOrderNotFoundException extends RuntimeException {
    public ServiceOrderNotFoundException(Long id) {
        super("Service order not found: " + id);
    }
}

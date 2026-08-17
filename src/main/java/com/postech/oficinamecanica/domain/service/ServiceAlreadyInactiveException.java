package com.postech.oficinamecanica.domain.service;

public class ServiceAlreadyInactiveException extends RuntimeException {
    public ServiceAlreadyInactiveException(Long id) {
        super("Service already inactive: " + id);
    }
}

package com.postech.oficinamecanica.domain.service;

public class ServiceNotFoundException extends RuntimeException {
    public ServiceNotFoundException(Long id) {
        super("Service not found: " + id);
    }
}

package com.postech.oficinamecanica.domain.service;

public class ServiceAlreadyActiveException extends RuntimeException {
    public ServiceAlreadyActiveException(Long id) {
        super("Service already active: " + id);
    }
}

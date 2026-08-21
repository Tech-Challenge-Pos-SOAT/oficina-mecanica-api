package com.postech.oficinamecanica.domain.service;

public class DuplicateServiceNameException extends RuntimeException {
    public DuplicateServiceNameException(String name) {
        super("Service name already exists: " + name);
    }
}

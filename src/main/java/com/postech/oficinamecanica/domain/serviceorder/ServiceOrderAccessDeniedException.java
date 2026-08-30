package com.postech.oficinamecanica.domain.serviceorder;

public class ServiceOrderAccessDeniedException extends RuntimeException {
    public ServiceOrderAccessDeniedException(Long id) {
        super("Service order " + id + " does not belong to the informed document");
    }
}

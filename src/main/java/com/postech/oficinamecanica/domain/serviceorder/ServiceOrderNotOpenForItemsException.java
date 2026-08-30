package com.postech.oficinamecanica.domain.serviceorder;

public class ServiceOrderNotOpenForItemsException extends RuntimeException {
    public ServiceOrderNotOpenForItemsException(Long id, ServiceOrderStatus status) {
        super("Service order " + id + " does not accept items while in status " + status);
    }
}

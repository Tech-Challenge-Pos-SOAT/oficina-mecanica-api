package com.postech.oficinamecanica.domain.serviceorder;

public class InvalidServiceOrderStatusException extends RuntimeException {
    public InvalidServiceOrderStatusException(String value) {
        super("Invalid service order status: " + value);
    }
}

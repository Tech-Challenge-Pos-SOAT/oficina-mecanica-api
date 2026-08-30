package com.postech.oficinamecanica.domain.serviceorder;

public class InvalidServiceOrderTransitionException extends RuntimeException {
    public InvalidServiceOrderTransitionException(Long id, ServiceOrderStatus from, ServiceOrderStatus to) {
        super("Service order " + id + " cannot move from " + from + " to " + to);
    }
}

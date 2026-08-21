package com.postech.oficinamecanica.domain.vehicle;

public class CustomerNotActiveException extends RuntimeException {
    public CustomerNotActiveException(Long customerId) {
        super("Customer not active: " + customerId);
    }
}

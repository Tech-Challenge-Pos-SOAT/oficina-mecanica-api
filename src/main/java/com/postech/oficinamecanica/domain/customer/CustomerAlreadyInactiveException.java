package com.postech.oficinamecanica.domain.customer;

public class CustomerAlreadyInactiveException extends RuntimeException {
    public CustomerAlreadyInactiveException(Long id) {
        super("Customer already inactive: " + id);
    }
}

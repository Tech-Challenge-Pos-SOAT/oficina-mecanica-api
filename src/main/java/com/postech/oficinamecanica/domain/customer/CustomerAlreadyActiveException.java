package com.postech.oficinamecanica.domain.customer;

public class CustomerAlreadyActiveException extends RuntimeException {
    public CustomerAlreadyActiveException(Long id) {
        super("Customer already active: " + id);
    }
}

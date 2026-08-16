package com.postech.oficinamecanica.domain.customer;

public class CustomerNotFoundException extends RuntimeException {
    public CustomerNotFoundException(Long id) {
        super("Customer not found: " + id);
    }

    public CustomerNotFoundException(String document) {
        super("Customer not found for document: " + document);
    }
}

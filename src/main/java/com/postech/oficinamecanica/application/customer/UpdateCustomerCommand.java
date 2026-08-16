package com.postech.oficinamecanica.application.customer;

public record UpdateCustomerCommand(
    Long id,
    String name,
    String phone,
    String email
) {}

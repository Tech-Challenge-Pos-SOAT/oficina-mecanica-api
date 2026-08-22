package com.postech.oficinamecanica.application.customer;

public record CreateCustomerCommand(
    String document,
    String name,
    String phone,
    String email
) {}

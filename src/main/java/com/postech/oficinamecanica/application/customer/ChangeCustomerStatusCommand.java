package com.postech.oficinamecanica.application.customer;

public record ChangeCustomerStatusCommand(
    Long id,
    String status
) {}

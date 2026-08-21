package com.postech.oficinamecanica.application.employee;

public record UpdateEmployeeCommand(
    Long id,
    String name,
    String email,
    String role
) {}

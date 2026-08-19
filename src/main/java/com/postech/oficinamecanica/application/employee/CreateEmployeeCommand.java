package com.postech.oficinamecanica.application.employee;

public record CreateEmployeeCommand(
    String name,
    String email,
    String password,
    String role
) {}

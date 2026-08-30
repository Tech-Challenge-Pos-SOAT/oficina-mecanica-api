package com.postech.oficinamecanica.application.serviceorder;

public record OpenServiceOrderCommand(
    String customerDocument,
    String plate,
    Long employeeId
) {}

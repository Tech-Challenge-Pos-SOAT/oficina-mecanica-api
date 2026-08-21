package com.postech.oficinamecanica.application.vehicle;

public record CreateVehicleCommand(
    Long customerId,
    String brand,
    String model,
    String plate,
    int year
) {}

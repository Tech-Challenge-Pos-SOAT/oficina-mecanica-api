package com.postech.oficinamecanica.application.vehicle;

public record UpdateVehicleCommand(
    Long id,
    String brand,
    String model,
    String plate,
    int year
) {}

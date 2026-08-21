package com.postech.oficinamecanica.application.vehicle;

public record ChangeVehicleStatusCommand(
    Long id,
    String status
) {}

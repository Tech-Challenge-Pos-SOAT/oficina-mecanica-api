package com.postech.oficinamecanica.application.vehicle;

public record TransferVehicleOwnershipCommand(
    Long vehicleId,
    String newOwnerDocument,
    Long employeeId
) {}

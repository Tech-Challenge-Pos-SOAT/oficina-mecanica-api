package com.postech.oficinamecanica.domain.vehicle;

public class VehicleNotFoundException extends RuntimeException {
    public VehicleNotFoundException(Long id) {
        super("Vehicle not found: " + id);
    }

    public VehicleNotFoundException(String plate) {
        super("Vehicle not found for plate: " + plate);
    }
}

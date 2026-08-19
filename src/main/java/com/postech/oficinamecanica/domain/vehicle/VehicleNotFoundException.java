package com.postech.oficinamecanica.domain.vehicle;

public class VehicleNotFoundException extends RuntimeException {
    public VehicleNotFoundException(Long id) {
        super("Vehicle not found: " + id);
    }
}

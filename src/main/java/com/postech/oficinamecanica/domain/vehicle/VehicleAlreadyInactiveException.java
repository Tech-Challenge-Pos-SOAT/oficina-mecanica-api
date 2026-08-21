package com.postech.oficinamecanica.domain.vehicle;

public class VehicleAlreadyInactiveException extends RuntimeException {
    public VehicleAlreadyInactiveException(Long id) {
        super("Vehicle already inactive: " + id);
    }
}

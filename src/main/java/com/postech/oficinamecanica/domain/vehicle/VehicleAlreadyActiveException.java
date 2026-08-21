package com.postech.oficinamecanica.domain.vehicle;

public class VehicleAlreadyActiveException extends RuntimeException {
    public VehicleAlreadyActiveException(Long id) {
        super("Vehicle already active: " + id);
    }
}

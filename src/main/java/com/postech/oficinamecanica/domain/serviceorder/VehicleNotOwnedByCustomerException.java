package com.postech.oficinamecanica.domain.serviceorder;

public class VehicleNotOwnedByCustomerException extends RuntimeException {
    public VehicleNotOwnedByCustomerException(Long vehicleId, Long customerId) {
        super("Vehicle " + vehicleId + " does not belong to customer " + customerId);
    }
}

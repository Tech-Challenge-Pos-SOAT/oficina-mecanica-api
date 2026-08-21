package com.postech.oficinamecanica.application.vehicle;

import com.postech.oficinamecanica.domain.shared.EntityStatus;
import com.postech.oficinamecanica.domain.vehicle.Plate;
import com.postech.oficinamecanica.domain.vehicle.Vehicle;
import java.util.List;
import java.util.Optional;

public interface VehicleRepository {
    List<Vehicle> findByStatus(EntityStatus status);
    Vehicle save(Vehicle vehicle);
    Optional<Vehicle> findById(Long id);
    Optional<Vehicle> findByPlate(Plate plate);
}

package com.postech.oficinamecanica.application.vehicle;

import com.postech.oficinamecanica.domain.shared.EntityStatus;
import com.postech.oficinamecanica.domain.vehicle.Vehicle;
import com.postech.oficinamecanica.domain.vehicle.VehicleNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class ChangeVehicleStatusUseCase {
    private final VehicleRepository repository;

    public ChangeVehicleStatusUseCase(VehicleRepository repository) {
        this.repository = repository;
    }

    public Vehicle execute(ChangeVehicleStatusCommand cmd) {
        Vehicle vehicle = repository.findById(cmd.id())
            .orElseThrow(() -> new VehicleNotFoundException(cmd.id()));

        EntityStatus targetStatus = EntityStatus.valueOf(cmd.status().toUpperCase());
        if (targetStatus == EntityStatus.ACTIVE) {
            vehicle.activate();
        } else {
            vehicle.deactivate();
        }

        return repository.save(vehicle);
    }
}

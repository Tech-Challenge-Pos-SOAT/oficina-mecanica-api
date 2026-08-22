package com.postech.oficinamecanica.application.vehicle;

import com.postech.oficinamecanica.domain.vehicle.DuplicatePlateException;
import com.postech.oficinamecanica.domain.vehicle.Plate;
import com.postech.oficinamecanica.domain.vehicle.Vehicle;
import com.postech.oficinamecanica.domain.vehicle.VehicleNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UpdateVehicleUseCase {
    private final VehicleRepository repository;

    public UpdateVehicleUseCase(VehicleRepository repository) {
        this.repository = repository;
    }

    public Vehicle execute(UpdateVehicleCommand cmd) {
        Vehicle vehicle = repository.findById(cmd.id())
            .orElseThrow(() -> new VehicleNotFoundException(cmd.id()));

        Plate plate = new Plate(cmd.plate());
        repository.findByPlate(plate)
            .filter(existing -> !existing.getId().equals(cmd.id()))
            .ifPresent(v -> { throw new DuplicatePlateException(plate); });

        vehicle.updateDetails(plate, cmd.brand(), cmd.model(), cmd.year());
        return repository.save(vehicle);
    }
}

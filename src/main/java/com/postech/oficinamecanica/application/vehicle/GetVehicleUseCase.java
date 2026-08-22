package com.postech.oficinamecanica.application.vehicle;

import com.postech.oficinamecanica.domain.vehicle.Vehicle;
import com.postech.oficinamecanica.domain.vehicle.VehicleNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class GetVehicleUseCase {
    private final VehicleRepository repository;

    public GetVehicleUseCase(VehicleRepository repository) {
        this.repository = repository;
    }

    public Vehicle execute(Long id) {
        return repository.findById(id)
            .orElseThrow(() -> new VehicleNotFoundException(id));
    }
}

package com.postech.oficinamecanica.application.vehicle;

import com.postech.oficinamecanica.domain.shared.EntityStatus;
import com.postech.oficinamecanica.domain.vehicle.Vehicle;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ListVehiclesUseCase {
    private final VehicleRepository repository;

    public ListVehiclesUseCase(VehicleRepository repository) {
        this.repository = repository;
    }

    public List<Vehicle> execute(String statusParam) {
        EntityStatus status = (statusParam == null || statusParam.isBlank())
            ? EntityStatus.ACTIVE
            : EntityStatus.valueOf(statusParam.toUpperCase());

        return repository.findByStatus(status);
    }
}

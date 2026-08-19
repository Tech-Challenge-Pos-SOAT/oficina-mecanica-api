package com.postech.oficinamecanica.application.vehicle;

import com.postech.oficinamecanica.application.customer.CustomerRepository;
import com.postech.oficinamecanica.domain.customer.Customer;
import com.postech.oficinamecanica.domain.customer.CustomerNotFoundException;
import com.postech.oficinamecanica.domain.shared.EntityStatus;
import com.postech.oficinamecanica.domain.vehicle.CustomerNotActiveException;
import com.postech.oficinamecanica.domain.vehicle.DuplicatePlateException;
import com.postech.oficinamecanica.domain.vehicle.Plate;
import com.postech.oficinamecanica.domain.vehicle.Vehicle;
import org.springframework.stereotype.Service;

@Service
public class CreateVehicleUseCase {
    private final VehicleRepository repository;
    private final CustomerRepository customerRepository;

    public CreateVehicleUseCase(VehicleRepository repository, CustomerRepository customerRepository) {
        this.repository = repository;
        this.customerRepository = customerRepository;
    }

    public Vehicle execute(CreateVehicleCommand cmd) {
        Customer customer = customerRepository.findById(cmd.customerId())
            .orElseThrow(() -> new CustomerNotFoundException(cmd.customerId()));
        if (customer.getStatus() != EntityStatus.ACTIVE) {
            throw new CustomerNotActiveException(cmd.customerId());
        }

        Plate plate = new Plate(cmd.plate());
        repository.findByPlate(plate)
            .ifPresent(v -> { throw new DuplicatePlateException(plate); });

        Vehicle vehicle = Vehicle.create(cmd.customerId(), plate, cmd.brand(), cmd.model(), cmd.year());
        return repository.save(vehicle);
    }
}

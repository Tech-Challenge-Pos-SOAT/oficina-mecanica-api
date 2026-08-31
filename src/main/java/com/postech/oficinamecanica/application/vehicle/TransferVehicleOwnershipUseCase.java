package com.postech.oficinamecanica.application.vehicle;

import com.postech.oficinamecanica.application.customer.CustomerRepository;
import com.postech.oficinamecanica.application.serviceorder.ActiveEmployeeFinder;
import com.postech.oficinamecanica.domain.customer.Customer;
import com.postech.oficinamecanica.domain.customer.CustomerNotFoundException;
import com.postech.oficinamecanica.domain.customer.Document;
import com.postech.oficinamecanica.domain.shared.EntityStatus;
import com.postech.oficinamecanica.domain.shared.exceptions.BusinessRuleViolationException;
import com.postech.oficinamecanica.domain.vehicle.CustomerNotActiveException;
import com.postech.oficinamecanica.domain.vehicle.Vehicle;
import com.postech.oficinamecanica.domain.vehicle.VehicleNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Segundo dono: o veiculo passa a ser do novo cliente, identificado por
 * CPF/CNPJ. As ordens antigas continuam com o dono da epoca, porque a OS
 * congela o cliente na abertura.
 */
@Service
public class TransferVehicleOwnershipUseCase {
    private final VehicleRepository vehicleRepository;
    private final CustomerRepository customerRepository;
    private final ActiveEmployeeFinder activeEmployeeFinder;

    public TransferVehicleOwnershipUseCase(VehicleRepository vehicleRepository,
                                            CustomerRepository customerRepository,
                                            ActiveEmployeeFinder activeEmployeeFinder) {
        this.vehicleRepository = vehicleRepository;
        this.customerRepository = customerRepository;
        this.activeEmployeeFinder = activeEmployeeFinder;
    }

    @Transactional
    public Vehicle execute(TransferVehicleOwnershipCommand command) {
        activeEmployeeFinder.findActive(command.employeeId());

        Vehicle vehicle = vehicleRepository.findById(command.vehicleId())
            .orElseThrow(() -> new VehicleNotFoundException(command.vehicleId()));

        Document document = new Document(command.newOwnerDocument());
        Customer newOwner = customerRepository.findByDocument(document)
            .orElseThrow(() -> new CustomerNotFoundException(command.newOwnerDocument()));

        if (newOwner.getStatus() != EntityStatus.ACTIVE) {
            throw new CustomerNotActiveException(newOwner.getId());
        }
        if (vehicle.getCustomerId().equals(newOwner.getId())) {
            throw new BusinessRuleViolationException(
                "Veiculo " + vehicle.getId() + " ja pertence ao cliente " + newOwner.getId());
        }

        vehicle.transferTo(newOwner.getId());
        return vehicleRepository.save(vehicle);
    }
}

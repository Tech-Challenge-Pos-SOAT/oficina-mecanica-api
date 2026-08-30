package com.postech.oficinamecanica.application.serviceorder;

import com.postech.oficinamecanica.application.customer.CustomerRepository;
import com.postech.oficinamecanica.application.vehicle.VehicleRepository;
import com.postech.oficinamecanica.domain.customer.Customer;
import com.postech.oficinamecanica.domain.customer.CustomerNotFoundException;
import com.postech.oficinamecanica.domain.customer.Document;
import com.postech.oficinamecanica.domain.serviceorder.ServiceOrder;
import com.postech.oficinamecanica.domain.serviceorder.VehicleNotOwnedByCustomerException;
import com.postech.oficinamecanica.domain.shared.EntityStatus;
import com.postech.oficinamecanica.domain.vehicle.CustomerNotActiveException;
import com.postech.oficinamecanica.domain.vehicle.Plate;
import com.postech.oficinamecanica.domain.vehicle.Vehicle;
import com.postech.oficinamecanica.domain.vehicle.VehicleNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Abertura da OS: identifica o cliente por CPF/CNPJ, o veiculo pela placa e
 * congela o dono vigente na ordem (o veiculo pode trocar de dono depois).
 */
@Service
public class OpenServiceOrderUseCase {
    private final ServiceOrderRepository serviceOrderRepository;
    private final CustomerRepository customerRepository;
    private final VehicleRepository vehicleRepository;
    private final ActiveEmployeeFinder activeEmployeeFinder;

    public OpenServiceOrderUseCase(ServiceOrderRepository serviceOrderRepository,
                                   CustomerRepository customerRepository,
                                   VehicleRepository vehicleRepository,
                                   ActiveEmployeeFinder activeEmployeeFinder) {
        this.serviceOrderRepository = serviceOrderRepository;
        this.customerRepository = customerRepository;
        this.vehicleRepository = vehicleRepository;
        this.activeEmployeeFinder = activeEmployeeFinder;
    }

    @Transactional
    public ServiceOrder execute(OpenServiceOrderCommand command) {
        activeEmployeeFinder.findActive(command.employeeId());

        Document document = new Document(command.customerDocument());
        Customer customer = customerRepository.findByDocument(document)
            .orElseThrow(() -> new CustomerNotFoundException(command.customerDocument()));

        if (customer.getStatus() != EntityStatus.ACTIVE) {
            throw new CustomerNotActiveException(customer.getId());
        }

        Plate plate = new Plate(command.plate());
        Vehicle vehicle = vehicleRepository.findByPlate(plate)
            .orElseThrow(() -> new VehicleNotFoundException(plate.value()));

        if (!vehicle.getCustomerId().equals(customer.getId())) {
            throw new VehicleNotOwnedByCustomerException(vehicle.getId(), customer.getId());
        }

        return serviceOrderRepository.save(
            ServiceOrder.open(customer.getId(), vehicle.getId(), command.employeeId()));
    }
}

package com.postech.oficinamecanica.application.serviceorder;

import com.postech.oficinamecanica.application.customer.CustomerRepository;
import com.postech.oficinamecanica.application.material.MaterialRepository;
import com.postech.oficinamecanica.application.service.ServiceRepository;
import com.postech.oficinamecanica.application.vehicle.VehicleRepository;
import com.postech.oficinamecanica.domain.customer.Customer;
import com.postech.oficinamecanica.domain.customer.CustomerNotFoundException;
import com.postech.oficinamecanica.domain.customer.Document;
import com.postech.oficinamecanica.domain.material.InactiveMaterialException;
import com.postech.oficinamecanica.domain.material.Material;
import com.postech.oficinamecanica.domain.service.InactiveServiceException;
import com.postech.oficinamecanica.domain.service.ServiceNotFoundException;
import com.postech.oficinamecanica.domain.serviceorder.ServiceOrder;
import com.postech.oficinamecanica.domain.serviceorder.VehicleNotOwnedByCustomerException;
import com.postech.oficinamecanica.domain.shared.EntityStatus;
import com.postech.oficinamecanica.domain.shared.exceptions.ResourceNotFoundException;
import com.postech.oficinamecanica.domain.vehicle.CustomerNotActiveException;
import com.postech.oficinamecanica.domain.vehicle.Plate;
import com.postech.oficinamecanica.domain.vehicle.Vehicle;
import com.postech.oficinamecanica.domain.vehicle.VehicleNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Abertura da OS: identifica o cliente por CPF/CNPJ, o veiculo pela placa e
 * congela o dono vigente na ordem (o veiculo pode trocar de dono depois).
 * Servicos e pecas informados na abertura entram na mesma transacao, com preco
 * copiado do catalogo; qualquer item invalido desfaz a abertura inteira.
 */
@Service
public class OpenServiceOrderUseCase {
    private final ServiceOrderRepository serviceOrderRepository;
    private final CustomerRepository customerRepository;
    private final VehicleRepository vehicleRepository;
    private final ActiveEmployeeFinder activeEmployeeFinder;
    private final ServiceRepository serviceRepository;
    private final MaterialRepository materialRepository;

    public OpenServiceOrderUseCase(ServiceOrderRepository serviceOrderRepository,
                                   CustomerRepository customerRepository,
                                   VehicleRepository vehicleRepository,
                                   ActiveEmployeeFinder activeEmployeeFinder,
                                   ServiceRepository serviceRepository,
                                   MaterialRepository materialRepository) {
        this.serviceOrderRepository = serviceOrderRepository;
        this.customerRepository = customerRepository;
        this.vehicleRepository = vehicleRepository;
        this.activeEmployeeFinder = activeEmployeeFinder;
        this.serviceRepository = serviceRepository;
        this.materialRepository = materialRepository;
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

        ServiceOrder order = ServiceOrder.open(customer.getId(), vehicle.getId(), command.employeeId());

        for (Long serviceId : command.serviceIds()) {
            com.postech.oficinamecanica.domain.service.Service service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new ServiceNotFoundException(serviceId));

            if (service.getStatus() != EntityStatus.ACTIVE) {
                throw new InactiveServiceException(serviceId);
            }

            order.addService(service.getId(), service.getPrice());
        }

        for (OpenServiceOrderCommand.MaterialItem item : command.materials()) {
            Material material = materialRepository.findById(item.materialId())
                .orElseThrow(() -> new ResourceNotFoundException("Material", item.materialId()));

            if (material.getStatus() != EntityStatus.ACTIVE) {
                throw new InactiveMaterialException(
                    "Cannot add inactive material " + item.materialId() + " to a service order");
            }

            order.addMaterial(material.getId(), item.quantity(), material.getPrice());
        }

        return serviceOrderRepository.save(order);
    }
}

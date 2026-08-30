package com.postech.oficinamecanica.application.serviceorder;

import com.postech.oficinamecanica.application.service.ServiceRepository;
import com.postech.oficinamecanica.domain.service.InactiveServiceException;
import com.postech.oficinamecanica.domain.service.ServiceNotFoundException;
import com.postech.oficinamecanica.domain.serviceorder.ServiceOrder;
import com.postech.oficinamecanica.domain.serviceorder.ServiceOrderNotFoundException;
import com.postech.oficinamecanica.domain.shared.EntityStatus;
import org.springframework.transaction.annotation.Transactional;

/**
 * Inclusao de servico solicitado. Copia o preco do catalogo para a ordem -
 * mudanca posterior no catalogo nao mexe em ordem existente.
 */
@org.springframework.stereotype.Service
public class AddServiceToServiceOrderUseCase {
    private final ServiceOrderRepository serviceOrderRepository;
    private final ServiceRepository serviceRepository;
    private final ActiveEmployeeFinder activeEmployeeFinder;

    public AddServiceToServiceOrderUseCase(ServiceOrderRepository serviceOrderRepository,
                                           ServiceRepository serviceRepository,
                                           ActiveEmployeeFinder activeEmployeeFinder) {
        this.serviceOrderRepository = serviceOrderRepository;
        this.serviceRepository = serviceRepository;
        this.activeEmployeeFinder = activeEmployeeFinder;
    }

    @Transactional
    public ServiceOrder execute(Long serviceOrderId, Long serviceId, Long employeeId) {
        activeEmployeeFinder.findActive(employeeId);

        ServiceOrder order = serviceOrderRepository.findById(serviceOrderId)
            .orElseThrow(() -> new ServiceOrderNotFoundException(serviceOrderId));

        com.postech.oficinamecanica.domain.service.Service service = serviceRepository.findById(serviceId)
            .orElseThrow(() -> new ServiceNotFoundException(serviceId));

        if (service.getStatus() != EntityStatus.ACTIVE) {
            throw new InactiveServiceException(serviceId);
        }

        order.addService(service.getId(), service.getPrice());
        return serviceOrderRepository.save(order);
    }
}

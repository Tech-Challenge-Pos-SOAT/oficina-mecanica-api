package com.postech.oficinamecanica.application.serviceorder;

import com.postech.oficinamecanica.domain.serviceorder.ServiceOrder;
import com.postech.oficinamecanica.domain.serviceorder.ServiceOrderNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DeliverServiceOrderUseCase {
    private final ServiceOrderRepository repository;
    private final ActiveEmployeeFinder activeEmployeeFinder;

    public DeliverServiceOrderUseCase(ServiceOrderRepository repository, ActiveEmployeeFinder activeEmployeeFinder) {
        this.repository = repository;
        this.activeEmployeeFinder = activeEmployeeFinder;
    }

    @Transactional
    public ServiceOrder execute(Long serviceOrderId, Long employeeId) {
        activeEmployeeFinder.findActive(employeeId);

        ServiceOrder order = repository.findById(serviceOrderId)
            .orElseThrow(() -> new ServiceOrderNotFoundException(serviceOrderId));

        order.deliver(employeeId);
        return repository.save(order);
    }
}

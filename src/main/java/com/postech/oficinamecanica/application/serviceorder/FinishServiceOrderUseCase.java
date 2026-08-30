package com.postech.oficinamecanica.application.serviceorder;

import com.postech.oficinamecanica.domain.serviceorder.ServiceOrder;
import com.postech.oficinamecanica.domain.serviceorder.ServiceOrderNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FinishServiceOrderUseCase {
    private final ServiceOrderRepository repository;
    private final ActiveEmployeeFinder activeEmployeeFinder;

    public FinishServiceOrderUseCase(ServiceOrderRepository repository, ActiveEmployeeFinder activeEmployeeFinder) {
        this.repository = repository;
        this.activeEmployeeFinder = activeEmployeeFinder;
    }

    @Transactional
    public ServiceOrder execute(Long serviceOrderId, Long employeeId, String observation) {
        activeEmployeeFinder.findActive(employeeId);

        ServiceOrder order = repository.findById(serviceOrderId)
            .orElseThrow(() -> new ServiceOrderNotFoundException(serviceOrderId));

        order.finish(employeeId, observation);
        return repository.save(order);
    }
}

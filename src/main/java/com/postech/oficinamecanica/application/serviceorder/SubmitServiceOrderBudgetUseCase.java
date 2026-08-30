package com.postech.oficinamecanica.application.serviceorder;

import com.postech.oficinamecanica.domain.serviceorder.ServiceOrder;
import com.postech.oficinamecanica.domain.serviceorder.ServiceOrderNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Envio do orcamento ao cliente: leva a ordem para AWAITING_APPROVAL. */
@Service
public class SubmitServiceOrderBudgetUseCase {
    private final ServiceOrderRepository repository;
    private final ActiveEmployeeFinder activeEmployeeFinder;

    public SubmitServiceOrderBudgetUseCase(ServiceOrderRepository repository, ActiveEmployeeFinder activeEmployeeFinder) {
        this.repository = repository;
        this.activeEmployeeFinder = activeEmployeeFinder;
    }

    @Transactional
    public ServiceOrder execute(Long serviceOrderId, Long employeeId) {
        activeEmployeeFinder.findActive(employeeId);

        ServiceOrder order = repository.findById(serviceOrderId)
            .orElseThrow(() -> new ServiceOrderNotFoundException(serviceOrderId));

        order.submitForApproval(employeeId);
        return repository.save(order);
    }
}

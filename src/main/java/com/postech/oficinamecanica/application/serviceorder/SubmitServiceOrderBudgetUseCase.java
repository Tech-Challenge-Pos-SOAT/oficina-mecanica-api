package com.postech.oficinamecanica.application.serviceorder;

import com.postech.oficinamecanica.application.customer.GetCustomerUseCase;
import com.postech.oficinamecanica.domain.customer.Customer;
import com.postech.oficinamecanica.domain.serviceorder.ServiceOrder;
import com.postech.oficinamecanica.domain.serviceorder.ServiceOrderNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Envio do orcamento ao cliente: leva a ordem para AWAITING_APPROVAL. */
@Service
public class SubmitServiceOrderBudgetUseCase {
    private final ServiceOrderRepository repository;
    private final ActiveEmployeeFinder activeEmployeeFinder;
    private final GetCustomerUseCase getCustomerUseCase;
    private final ServiceOrderNotifier notifier;

    public SubmitServiceOrderBudgetUseCase(ServiceOrderRepository repository, ActiveEmployeeFinder activeEmployeeFinder,
                                            GetCustomerUseCase getCustomerUseCase, ServiceOrderNotifier notifier) {
        this.repository = repository;
        this.activeEmployeeFinder = activeEmployeeFinder;
        this.getCustomerUseCase = getCustomerUseCase;
        this.notifier = notifier;
    }

    @Transactional
    public ServiceOrder execute(Long serviceOrderId, Long employeeId) {
        activeEmployeeFinder.findActive(employeeId);

        ServiceOrder order = repository.findById(serviceOrderId)
            .orElseThrow(() -> new ServiceOrderNotFoundException(serviceOrderId));

        order.submitForApproval(employeeId);
        ServiceOrder saved = repository.save(order);

        Customer customer = getCustomerUseCase.execute(saved.getCustomerId());
        notifier.notifyBudgetSubmitted(saved, customer);
        return saved;
    }
}

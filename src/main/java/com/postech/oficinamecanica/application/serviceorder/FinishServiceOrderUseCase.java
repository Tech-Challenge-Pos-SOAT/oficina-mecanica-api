package com.postech.oficinamecanica.application.serviceorder;

import com.postech.oficinamecanica.application.customer.GetCustomerUseCase;
import com.postech.oficinamecanica.domain.customer.Customer;
import com.postech.oficinamecanica.domain.serviceorder.ServiceOrder;
import com.postech.oficinamecanica.domain.serviceorder.ServiceOrderNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FinishServiceOrderUseCase {
    private final ServiceOrderRepository repository;
    private final ActiveEmployeeFinder activeEmployeeFinder;
    private final GetCustomerUseCase getCustomerUseCase;
    private final ServiceOrderNotifier notifier;

    public FinishServiceOrderUseCase(ServiceOrderRepository repository, ActiveEmployeeFinder activeEmployeeFinder,
                                      GetCustomerUseCase getCustomerUseCase, ServiceOrderNotifier notifier) {
        this.repository = repository;
        this.activeEmployeeFinder = activeEmployeeFinder;
        this.getCustomerUseCase = getCustomerUseCase;
        this.notifier = notifier;
    }

    @Transactional
    public ServiceOrder execute(Long serviceOrderId, Long employeeId, String observation) {
        activeEmployeeFinder.findActive(employeeId);

        ServiceOrder order = repository.findById(serviceOrderId)
            .orElseThrow(() -> new ServiceOrderNotFoundException(serviceOrderId));

        order.finish(employeeId, observation);
        ServiceOrder saved = repository.save(order);

        Customer customer = getCustomerUseCase.execute(saved.getCustomerId());
        notifier.notifyFinished(saved, customer);
        return saved;
    }
}

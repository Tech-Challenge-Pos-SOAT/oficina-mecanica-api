package com.postech.oficinamecanica.application.serviceorder;

import com.postech.oficinamecanica.application.customer.GetCustomerByDocumentUseCase;
import com.postech.oficinamecanica.domain.customer.Customer;
import com.postech.oficinamecanica.domain.serviceorder.ServiceOrder;
import com.postech.oficinamecanica.domain.serviceorder.ServiceOrderAccessDeniedException;
import com.postech.oficinamecanica.domain.serviceorder.ServiceOrderNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Recusa do orcamento: encerra a ordem sem consumir estoque. */
@Service
public class RejectServiceOrderBudgetUseCase {
    private final ServiceOrderRepository repository;
    private final GetCustomerByDocumentUseCase getCustomerByDocumentUseCase;
    private final ServiceOrderNotifier notifier;

    public RejectServiceOrderBudgetUseCase(ServiceOrderRepository repository,
                                            GetCustomerByDocumentUseCase getCustomerByDocumentUseCase,
                                            ServiceOrderNotifier notifier) {
        this.repository = repository;
        this.getCustomerByDocumentUseCase = getCustomerByDocumentUseCase;
        this.notifier = notifier;
    }

    @Transactional
    public ServiceOrder execute(Long serviceOrderId, String customerDocument, String reason) {
        ServiceOrder order = repository.findById(serviceOrderId)
            .orElseThrow(() -> new ServiceOrderNotFoundException(serviceOrderId));

        Customer customer = getCustomerByDocumentUseCase.execute(customerDocument);
        if (!order.getCustomerId().equals(customer.getId())) {
            throw new ServiceOrderAccessDeniedException(serviceOrderId);
        }

        order.rejectBudget(reason);
        ServiceOrder saved = repository.save(order);
        notifier.notifyBudgetRejected(saved, customer, reason);
        return saved;
    }
}

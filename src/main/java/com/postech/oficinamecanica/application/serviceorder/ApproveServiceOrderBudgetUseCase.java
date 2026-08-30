package com.postech.oficinamecanica.application.serviceorder;

import com.postech.oficinamecanica.application.customer.GetCustomerByDocumentUseCase;
import com.postech.oficinamecanica.application.material.StockDebitUseCase;
import com.postech.oficinamecanica.domain.customer.Customer;
import com.postech.oficinamecanica.domain.serviceorder.ServiceOrder;
import com.postech.oficinamecanica.domain.serviceorder.ServiceOrderAccessDeniedException;
import com.postech.oficinamecanica.domain.serviceorder.ServiceOrderMaterial;
import com.postech.oficinamecanica.domain.serviceorder.ServiceOrderNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Aprovacao do orcamento pelo cliente. A baixa de estoque acontece aqui e em
 * uma unica transacao: estoque insuficiente derruba tudo e a ordem continua em
 * AWAITING_APPROVAL.
 */
@Service
public class ApproveServiceOrderBudgetUseCase {
    private final ServiceOrderRepository repository;
    private final GetCustomerByDocumentUseCase getCustomerByDocumentUseCase;
    private final StockDebitUseCase stockDebitUseCase;

    public ApproveServiceOrderBudgetUseCase(ServiceOrderRepository repository,
                                            GetCustomerByDocumentUseCase getCustomerByDocumentUseCase,
                                            StockDebitUseCase stockDebitUseCase) {
        this.repository = repository;
        this.getCustomerByDocumentUseCase = getCustomerByDocumentUseCase;
        this.stockDebitUseCase = stockDebitUseCase;
    }

    @Transactional
    public ServiceOrder execute(Long serviceOrderId, String customerDocument) {
        ServiceOrder order = repository.findById(serviceOrderId)
            .orElseThrow(() -> new ServiceOrderNotFoundException(serviceOrderId));

        Customer customer = getCustomerByDocumentUseCase.execute(customerDocument);
        if (!order.getCustomerId().equals(customer.getId())) {
            throw new ServiceOrderAccessDeniedException(serviceOrderId);
        }

        order.approveBudget();

        for (ServiceOrderMaterial material : order.pendingStockDebit()) {
            stockDebitUseCase.execute(material.getMaterialId(), serviceOrderId, material.getQuantity());
        }
        order.markStockDebited();

        return repository.save(order);
    }
}

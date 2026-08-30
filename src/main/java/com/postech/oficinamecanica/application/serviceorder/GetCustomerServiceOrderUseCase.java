package com.postech.oficinamecanica.application.serviceorder;

import com.postech.oficinamecanica.application.customer.GetCustomerByDocumentUseCase;
import com.postech.oficinamecanica.domain.customer.Customer;
import com.postech.oficinamecanica.domain.serviceorder.ServiceOrder;
import com.postech.oficinamecanica.domain.serviceorder.ServiceOrderAccessDeniedException;
import com.postech.oficinamecanica.domain.serviceorder.ServiceOrderNotFoundException;
import org.springframework.stereotype.Service;

/** Detalhe de uma ordem para o cliente, provando posse pelo CPF/CNPJ. */
@Service
public class GetCustomerServiceOrderUseCase {
    private final ServiceOrderRepository repository;
    private final GetCustomerByDocumentUseCase getCustomerByDocumentUseCase;

    public GetCustomerServiceOrderUseCase(ServiceOrderRepository repository,
                                          GetCustomerByDocumentUseCase getCustomerByDocumentUseCase) {
        this.repository = repository;
        this.getCustomerByDocumentUseCase = getCustomerByDocumentUseCase;
    }

    public ServiceOrder execute(Long serviceOrderId, String customerDocument) {
        ServiceOrder order = repository.findById(serviceOrderId)
            .orElseThrow(() -> new ServiceOrderNotFoundException(serviceOrderId));

        Customer customer = getCustomerByDocumentUseCase.execute(customerDocument);
        if (!order.getCustomerId().equals(customer.getId())) {
            throw new ServiceOrderAccessDeniedException(serviceOrderId);
        }
        return order;
    }
}

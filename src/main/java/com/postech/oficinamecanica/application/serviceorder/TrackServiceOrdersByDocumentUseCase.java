package com.postech.oficinamecanica.application.serviceorder;

import com.postech.oficinamecanica.application.customer.GetCustomerByDocumentUseCase;
import com.postech.oficinamecanica.domain.customer.Customer;
import com.postech.oficinamecanica.domain.serviceorder.ServiceOrder;
import org.springframework.stereotype.Service;

import java.util.List;

/** Acompanhamento pelo cliente: lista as ordens dele a partir do CPF/CNPJ. */
@Service
public class TrackServiceOrdersByDocumentUseCase {
    private final ServiceOrderRepository repository;
    private final GetCustomerByDocumentUseCase getCustomerByDocumentUseCase;

    public TrackServiceOrdersByDocumentUseCase(ServiceOrderRepository repository,
                                               GetCustomerByDocumentUseCase getCustomerByDocumentUseCase) {
        this.repository = repository;
        this.getCustomerByDocumentUseCase = getCustomerByDocumentUseCase;
    }

    public List<ServiceOrder> execute(String customerDocument) {
        Customer customer = getCustomerByDocumentUseCase.execute(customerDocument);
        return repository.findByCustomerId(customer.getId());
    }
}

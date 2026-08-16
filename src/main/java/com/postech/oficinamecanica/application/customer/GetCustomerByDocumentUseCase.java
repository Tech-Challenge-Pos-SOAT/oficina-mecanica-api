package com.postech.oficinamecanica.application.customer;

import com.postech.oficinamecanica.domain.customer.Customer;
import com.postech.oficinamecanica.domain.customer.CustomerNotFoundException;
import com.postech.oficinamecanica.domain.customer.Document;
import org.springframework.stereotype.Service;

@Service
public class GetCustomerByDocumentUseCase {
    private final CustomerRepository repository;

    public GetCustomerByDocumentUseCase(CustomerRepository repository) {
        this.repository = repository;
    }

    public Customer execute(String documentValue) {
        Document document = new Document(documentValue);
        return repository.findByDocument(document)
            .orElseThrow(() -> new CustomerNotFoundException(documentValue));
    }
}

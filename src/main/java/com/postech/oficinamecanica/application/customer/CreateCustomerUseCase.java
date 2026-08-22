package com.postech.oficinamecanica.application.customer;

import com.postech.oficinamecanica.domain.customer.Customer;
import com.postech.oficinamecanica.domain.customer.Document;
import com.postech.oficinamecanica.domain.customer.DuplicateDocumentException;
import com.postech.oficinamecanica.domain.customer.DuplicateEmailException;
import org.springframework.stereotype.Service;

@Service
public class CreateCustomerUseCase {
    private final CustomerRepository repository;

    public CreateCustomerUseCase(CustomerRepository repository) {
        this.repository = repository;
    }

    public Customer execute(CreateCustomerCommand cmd) {
        Document document = new Document(cmd.document());

        repository.findByDocument(document)
            .ifPresent(c -> { throw new DuplicateDocumentException(document); });

        repository.findByEmail(cmd.email())
            .ifPresent(c -> { throw new DuplicateEmailException(cmd.email()); });

        Customer customer = Customer.create(document, cmd.name(), cmd.phone(), cmd.email());
        return repository.save(customer);
    }
}

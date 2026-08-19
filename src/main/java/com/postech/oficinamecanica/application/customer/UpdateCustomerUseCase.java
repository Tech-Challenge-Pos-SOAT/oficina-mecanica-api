package com.postech.oficinamecanica.application.customer;

import com.postech.oficinamecanica.domain.customer.Customer;
import com.postech.oficinamecanica.domain.customer.CustomerNotFoundException;
import com.postech.oficinamecanica.domain.customer.DuplicateEmailException;
import org.springframework.stereotype.Service;

@Service
public class UpdateCustomerUseCase {
    private final CustomerRepository repository;

    public UpdateCustomerUseCase(CustomerRepository repository) {
        this.repository = repository;
    }

    public Customer execute(UpdateCustomerCommand cmd) {
        Customer customer = repository.findById(cmd.id())
            .orElseThrow(() -> new CustomerNotFoundException(cmd.id()));

        repository.findByEmail(cmd.email())
            .filter(existing -> !existing.getId().equals(cmd.id()))
            .ifPresent(c -> { throw new DuplicateEmailException(cmd.email()); });

        customer.updateDetails(cmd.name(), cmd.phone(), cmd.email());
        return repository.save(customer);
    }
}

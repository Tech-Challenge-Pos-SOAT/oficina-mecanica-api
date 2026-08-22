package com.postech.oficinamecanica.application.customer;

import com.postech.oficinamecanica.domain.customer.Customer;
import com.postech.oficinamecanica.domain.customer.CustomerNotFoundException;
import com.postech.oficinamecanica.domain.shared.EntityStatus;
import org.springframework.stereotype.Service;

@Service
public class ChangeCustomerStatusUseCase {
    private final CustomerRepository repository;

    public ChangeCustomerStatusUseCase(CustomerRepository repository) {
        this.repository = repository;
    }

    public Customer execute(ChangeCustomerStatusCommand cmd) {
        Customer customer = repository.findById(cmd.id())
            .orElseThrow(() -> new CustomerNotFoundException(cmd.id()));

        EntityStatus targetStatus = EntityStatus.valueOf(cmd.status().toUpperCase());
        if (targetStatus == EntityStatus.ACTIVE) {
            customer.activate();
        } else {
            customer.deactivate();
        }

        return repository.save(customer);
    }
}

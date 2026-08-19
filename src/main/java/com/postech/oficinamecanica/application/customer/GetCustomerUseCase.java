package com.postech.oficinamecanica.application.customer;

import com.postech.oficinamecanica.domain.customer.Customer;
import com.postech.oficinamecanica.domain.customer.CustomerNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class GetCustomerUseCase {
    private final CustomerRepository repository;

    public GetCustomerUseCase(CustomerRepository repository) {
        this.repository = repository;
    }

    public Customer execute(Long id) {
        return repository.findById(id)
            .orElseThrow(() -> new CustomerNotFoundException(id));
    }
}

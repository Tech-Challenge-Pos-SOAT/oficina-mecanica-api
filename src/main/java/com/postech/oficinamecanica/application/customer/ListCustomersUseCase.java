package com.postech.oficinamecanica.application.customer;

import com.postech.oficinamecanica.domain.customer.Customer;
import com.postech.oficinamecanica.domain.shared.EntityStatus;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ListCustomersUseCase {
    private final CustomerRepository repository;

    public ListCustomersUseCase(CustomerRepository repository) {
        this.repository = repository;
    }

    public List<Customer> execute(String statusParam) {
        EntityStatus status = (statusParam == null || statusParam.isBlank())
            ? EntityStatus.ACTIVE
            : EntityStatus.valueOf(statusParam.toUpperCase());

        return repository.findByStatus(status);
    }
}

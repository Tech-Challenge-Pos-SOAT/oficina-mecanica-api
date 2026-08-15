package com.postech.oficinamecanica.application.customer;

import com.postech.oficinamecanica.domain.customer.Customer;
import com.postech.oficinamecanica.domain.shared.EntityStatus;
import java.util.List;

public interface CustomerRepository {
    List<Customer> findByStatus(EntityStatus status);
}

package com.postech.oficinamecanica.application.customer;

import com.postech.oficinamecanica.domain.customer.Customer;
import com.postech.oficinamecanica.domain.customer.Document;
import com.postech.oficinamecanica.domain.shared.EntityStatus;
import java.util.List;
import java.util.Optional;

public interface CustomerRepository {
    List<Customer> findByStatus(EntityStatus status);
    Customer save(Customer customer);
    Optional<Customer> findById(Long id);
    Optional<Customer> findByDocument(Document document);
    Optional<Customer> findByEmail(String email);
}

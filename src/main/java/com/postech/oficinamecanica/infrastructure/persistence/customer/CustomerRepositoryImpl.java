package com.postech.oficinamecanica.infrastructure.persistence.customer;

import com.postech.oficinamecanica.application.customer.CustomerRepository;
import com.postech.oficinamecanica.domain.customer.Customer;
import com.postech.oficinamecanica.domain.customer.Document;
import com.postech.oficinamecanica.domain.shared.EntityStatus;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public class CustomerRepositoryImpl implements CustomerRepository {
    private final CustomerJpaRepository jpaRepository;
    private final CustomerPersistenceMapper mapper;

    public CustomerRepositoryImpl(CustomerJpaRepository jpaRepository, CustomerPersistenceMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public List<Customer> findByStatus(EntityStatus status) {
        return jpaRepository.findByStatusOrderById(status)
            .stream()
            .map(mapper::toDomain)
            .toList();
    }

    @Override
    public Customer save(Customer customer) {
        return mapper.toDomain(jpaRepository.save(mapper.toPersistence(customer)));
    }

    @Override
    public Optional<Customer> findById(Long id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<Customer> findByDocument(Document document) {
        return jpaRepository.findByDocument(document.value()).map(mapper::toDomain);
    }

    @Override
    public Optional<Customer> findByEmail(String email) {
        return jpaRepository.findByEmail(email).map(mapper::toDomain);
    }
}

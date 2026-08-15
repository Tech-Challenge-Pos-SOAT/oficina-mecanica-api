package com.postech.oficinamecanica.infrastructure.persistence.customer;

import com.postech.oficinamecanica.application.customer.CustomerRepository;
import com.postech.oficinamecanica.domain.customer.Customer;
import com.postech.oficinamecanica.domain.shared.EntityStatus;
import org.springframework.stereotype.Repository;
import java.util.List;

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
}

package com.postech.oficinamecanica.infrastructure.persistence.serviceorder;

import com.postech.oficinamecanica.application.serviceorder.ServiceOrderRepository;
import com.postech.oficinamecanica.domain.serviceorder.ServiceOrder;
import com.postech.oficinamecanica.domain.serviceorder.ServiceOrderStatus;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public class ServiceOrderRepositoryImpl implements ServiceOrderRepository {
    private final ServiceOrderJpaRepository jpaRepository;
    private final ServiceOrderPersistenceMapper mapper;

    public ServiceOrderRepositoryImpl(ServiceOrderJpaRepository jpaRepository, ServiceOrderPersistenceMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public ServiceOrder save(ServiceOrder serviceOrder) {
        return mapper.toDomain(jpaRepository.save(mapper.toPersistence(serviceOrder)));
    }

    // As colecoes do agregado sao LAZY: a conversao para dominio precisa
    // acontecer com a sessao aberta (open-in-view esta desligado).
    @Override
    @Transactional(readOnly = true)
    public Optional<ServiceOrder> findById(Long id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ServiceOrder> findByCustomerId(Long customerId) {
        return jpaRepository.findByCustomerIdOrderByIdAsc(customerId)
            .stream()
            .map(mapper::toDomain)
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ServiceOrder> findAll(ServiceOrderStatus status) {
        List<ServiceOrderJpaEntity> entities = (status == null)
            ? jpaRepository.findAllByOrderByIdAsc()
            : jpaRepository.findByStatusOrderByIdAsc(status);

        return entities.stream()
            .map(mapper::toDomain)
            .toList();
    }
}

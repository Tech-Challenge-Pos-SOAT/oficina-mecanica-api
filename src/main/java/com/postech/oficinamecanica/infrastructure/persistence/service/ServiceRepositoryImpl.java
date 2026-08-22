package com.postech.oficinamecanica.infrastructure.persistence.service;

import com.postech.oficinamecanica.application.service.ServiceRepository;
import com.postech.oficinamecanica.domain.service.Service;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public class ServiceRepositoryImpl implements ServiceRepository {
    private final ServiceJpaRepository jpaRepository;
    private final ServicePersistenceMapper mapper;

    public ServiceRepositoryImpl(ServiceJpaRepository jpaRepository, ServicePersistenceMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Service save(Service service) {
        ServiceJpaEntity entity = mapper.toPersistence(service);
        ServiceJpaEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Service> findById(Long id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<Service> findByName(String name) {
        return jpaRepository.findByName(name).map(mapper::toDomain);
    }

    @Override
    public List<Service> findAll() {
        return jpaRepository.findAllByOrderByIdAsc()
            .stream()
            .map(mapper::toDomain)
            .toList();
    }
}

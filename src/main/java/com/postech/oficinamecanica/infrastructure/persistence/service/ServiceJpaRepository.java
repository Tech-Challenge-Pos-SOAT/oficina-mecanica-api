package com.postech.oficinamecanica.infrastructure.persistence.service;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ServiceJpaRepository extends JpaRepository<ServiceJpaEntity, Long> {
    Optional<ServiceJpaEntity> findByName(String name);
    List<ServiceJpaEntity> findAllByOrderByIdAsc();
}

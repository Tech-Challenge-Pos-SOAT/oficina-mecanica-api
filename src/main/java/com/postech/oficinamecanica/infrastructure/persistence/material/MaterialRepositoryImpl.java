package com.postech.oficinamecanica.infrastructure.persistence.material;

import com.postech.oficinamecanica.application.material.MaterialRepository;
import com.postech.oficinamecanica.domain.material.Material;
import com.postech.oficinamecanica.domain.shared.EntityStatus;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public class MaterialRepositoryImpl implements MaterialRepository {
    private final MaterialJpaRepository jpaRepository;
    private final MaterialPersistenceMapper mapper;

    public MaterialRepositoryImpl(MaterialJpaRepository jpaRepository, MaterialPersistenceMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public List<Material> findByStatus(EntityStatus status) {
        return jpaRepository.findByStatusOrderById(status)
            .stream()
            .map(mapper::toDomain)
            .toList();
    }
}

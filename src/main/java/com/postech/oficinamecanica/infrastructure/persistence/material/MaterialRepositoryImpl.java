package com.postech.oficinamecanica.infrastructure.persistence.material;

import com.postech.oficinamecanica.application.material.MaterialRepository;
import com.postech.oficinamecanica.domain.material.Material;
import com.postech.oficinamecanica.domain.shared.EntityStatus;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

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

    @Override
    public Optional<Material> findById(Long id) {
        return jpaRepository.findById(id)
            .map(mapper::toDomain);
    }

    @Override
    public Optional<Material> findByIdForUpdate(Long id) {
        return jpaRepository.findByIdForUpdate(id)
                .map(mapper::toDomain);
    }

    @Override
    public List<Material> findLowStockByStatus(EntityStatus status) {
        return jpaRepository.findLowStockByStatusOrderById(status)
            .stream()
            .map(mapper::toDomain)
            .toList();
    }

    @Override
    public Material save(Material material) {
        MaterialJpaEntity entity = mapper.toPersistence(material);
        MaterialJpaEntity savedEntity = jpaRepository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public boolean existsByName(String name) {
        return jpaRepository.existsByName(name);
    }
}

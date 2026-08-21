package com.postech.oficinamecanica.infrastructure.persistence.materialtransaction;

import com.postech.oficinamecanica.application.materialtransaction.MaterialTransactionRepository;
import com.postech.oficinamecanica.domain.materialtransaction.MaterialTransaction;
import com.postech.oficinamecanica.domain.materialtransaction.TransactionType;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public class MaterialTransactionRepositoryImpl implements MaterialTransactionRepository {
    private final MaterialTransactionJpaRepository jpaRepository;
    private final MaterialTransactionPersistenceMapper mapper;

    public MaterialTransactionRepositoryImpl(
        MaterialTransactionJpaRepository jpaRepository,
        MaterialTransactionPersistenceMapper mapper
    ) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public List<MaterialTransaction> findAll(TransactionType type) {
        List<MaterialTransactionJpaEntity> entities = (type == null)
                ? jpaRepository.findAllByOrderByIdAsc()
                : jpaRepository.findByTypeOrderByIdAsc(type);

        return entities.stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public MaterialTransaction save(MaterialTransaction transaction) {
        MaterialTransactionJpaEntity entity = mapper.toPersistence(transaction);
        MaterialTransactionJpaEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }
}

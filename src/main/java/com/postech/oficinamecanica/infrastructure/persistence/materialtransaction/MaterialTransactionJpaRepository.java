package com.postech.oficinamecanica.infrastructure.persistence.materialtransaction;

import com.postech.oficinamecanica.domain.materialtransaction.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface MaterialTransactionJpaRepository
    extends JpaRepository<MaterialTransactionJpaEntity, Long> {

    @Query("SELECT t FROM MaterialTransactionJpaEntity t WHERE (t.type = :type) ORDER BY t.id ASC")
    List<MaterialTransactionJpaEntity> findByTypeOrderByIdAsc(@Param("type") TransactionType type);

    @Query("SELECT t FROM MaterialTransactionJpaEntity t ORDER BY t.id ASC")
    List<MaterialTransactionJpaEntity> findAllByOrderByIdAsc();

    @Query("SELECT t FROM MaterialTransactionJpaEntity t WHERE t.materialId = :materialId ORDER BY t.id ASC")
    List<MaterialTransactionJpaEntity> findByMaterialIdOrderByIdAsc(@Param("materialId") Long materialId);

    @Query("SELECT t FROM MaterialTransactionJpaEntity t WHERE t.materialId = :materialId AND t.type = :type ORDER BY t.id ASC")
    List<MaterialTransactionJpaEntity> findByMaterialIdAndTypeOrderByIdAsc(
        @Param("materialId") Long materialId,
        @Param("type") TransactionType type
    );
}

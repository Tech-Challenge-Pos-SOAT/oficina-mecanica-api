package com.postech.oficinamecanica.infrastructure.persistence.material;

import com.postech.oficinamecanica.domain.shared.EntityStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface MaterialJpaRepository extends JpaRepository<MaterialJpaEntity, Long> {
    @Query("SELECT m FROM MaterialJpaEntity m WHERE m.status = :status ORDER BY m.id ASC")
    List<MaterialJpaEntity> findByStatusOrderById(@Param("status") EntityStatus status);

    @Query("SELECT m FROM MaterialJpaEntity m WHERE m.status = :status AND m.stockQuantity < m.stockMinimum ORDER BY m.id ASC")
    List<MaterialJpaEntity> findLowStockByStatusOrderById(@Param("status") EntityStatus status);
}

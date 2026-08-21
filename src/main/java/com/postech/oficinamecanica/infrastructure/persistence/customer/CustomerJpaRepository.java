package com.postech.oficinamecanica.infrastructure.persistence.customer;

import com.postech.oficinamecanica.domain.shared.EntityStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface CustomerJpaRepository extends JpaRepository<CustomerJpaEntity, Long> {
    @Query("SELECT c FROM CustomerJpaEntity c WHERE c.status = :status ORDER BY c.id ASC")
    List<CustomerJpaEntity> findByStatusOrderById(@Param("status") EntityStatus status);

    Optional<CustomerJpaEntity> findByDocument(String document);

    Optional<CustomerJpaEntity> findByEmail(String email);
}

package com.postech.oficinamecanica.infrastructure.persistence.serviceorder;

import com.postech.oficinamecanica.domain.serviceorder.ServiceOrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ServiceOrderJpaRepository extends JpaRepository<ServiceOrderJpaEntity, Long> {

    @Query("SELECT o FROM ServiceOrderJpaEntity o ORDER BY o.id ASC")
    List<ServiceOrderJpaEntity> findAllByOrderByIdAsc();

    @Query("SELECT o FROM ServiceOrderJpaEntity o WHERE o.status = :status ORDER BY o.id ASC")
    List<ServiceOrderJpaEntity> findByStatusOrderByIdAsc(@Param("status") ServiceOrderStatus status);

    @Query("SELECT o FROM ServiceOrderJpaEntity o WHERE o.customerId = :customerId ORDER BY o.id ASC")
    List<ServiceOrderJpaEntity> findByCustomerIdOrderByIdAsc(@Param("customerId") Long customerId);
}

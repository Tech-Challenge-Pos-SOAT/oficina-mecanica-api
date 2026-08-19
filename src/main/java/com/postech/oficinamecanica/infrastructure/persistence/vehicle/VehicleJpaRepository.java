package com.postech.oficinamecanica.infrastructure.persistence.vehicle;

import com.postech.oficinamecanica.domain.shared.EntityStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface VehicleJpaRepository extends JpaRepository<VehicleJpaEntity, Long> {
    @Query("SELECT v FROM VehicleJpaEntity v WHERE v.status = :status ORDER BY v.id ASC")
    List<VehicleJpaEntity> findByStatusOrderById(@Param("status") EntityStatus status);

    Optional<VehicleJpaEntity> findByPlate(String plate);
}

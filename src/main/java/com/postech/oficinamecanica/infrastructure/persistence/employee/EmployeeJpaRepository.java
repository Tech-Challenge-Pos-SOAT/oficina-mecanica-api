package com.postech.oficinamecanica.infrastructure.persistence.employee;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface EmployeeJpaRepository extends JpaRepository<EmployeeJpaEntity, Long> {
    Optional<EmployeeJpaEntity> findByEmail(String email);
}

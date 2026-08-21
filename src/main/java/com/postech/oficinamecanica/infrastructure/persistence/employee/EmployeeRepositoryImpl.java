package com.postech.oficinamecanica.infrastructure.persistence.employee;

import com.postech.oficinamecanica.application.employee.EmployeeRepository;
import com.postech.oficinamecanica.domain.employee.Employee;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public class EmployeeRepositoryImpl implements EmployeeRepository {
    private final EmployeeJpaRepository jpaRepository;
    private final EmployeePersistenceMapper mapper;

    public EmployeeRepositoryImpl(EmployeeJpaRepository jpaRepository, EmployeePersistenceMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Employee save(Employee employee) {
        EmployeeJpaEntity entity = mapper.toPersistence(employee);
        EmployeeJpaEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Employee> findById(Long id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<Employee> findByEmail(String email) {
        return jpaRepository.findByEmail(email).map(mapper::toDomain);
    }

    @Override
    public List<Employee> findAll() {
        return jpaRepository.findAll().stream().map(mapper::toDomain).toList();
    }
}

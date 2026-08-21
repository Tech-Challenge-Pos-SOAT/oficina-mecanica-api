package com.postech.oficinamecanica.application.employee;

import com.postech.oficinamecanica.domain.employee.Employee;
import com.postech.oficinamecanica.domain.employee.EmployeeNotFoundException;
import com.postech.oficinamecanica.domain.shared.EntityStatus;
import org.springframework.stereotype.Service;

@Service
public class UpdateEmployeeStatusUseCase {
    private final EmployeeRepository repository;

    public UpdateEmployeeStatusUseCase(EmployeeRepository repository) {
        this.repository = repository;
    }

    public Employee execute(Long id, String statusParam) {
        Employee employee = repository.findById(id)
            .orElseThrow(() -> new EmployeeNotFoundException(id));

        EntityStatus target = EntityStatus.valueOf(statusParam.toUpperCase());

        if (target == EntityStatus.ACTIVE) {
            employee.activate();
        } else {
            employee.deactivate();
        }

        return repository.save(employee);
    }
}

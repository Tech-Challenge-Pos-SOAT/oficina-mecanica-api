package com.postech.oficinamecanica.application.employee;

import com.postech.oficinamecanica.domain.employee.DuplicateEmailException;
import com.postech.oficinamecanica.domain.employee.Employee;
import com.postech.oficinamecanica.domain.employee.EmployeeNotFoundException;
import com.postech.oficinamecanica.domain.employee.EmployeeRole;
import org.springframework.stereotype.Service;

@Service
public class UpdateEmployeeUseCase {
    private final EmployeeRepository repository;

    public UpdateEmployeeUseCase(EmployeeRepository repository) {
        this.repository = repository;
    }

    public Employee execute(UpdateEmployeeCommand cmd) {
        Employee employee = repository.findById(cmd.id())
            .orElseThrow(() -> new EmployeeNotFoundException(cmd.id()));

        EmployeeRole role = EmployeeRole.fromValue(cmd.role());

        repository.findByEmail(cmd.email())
            .filter(existing -> !existing.getId().equals(cmd.id()))
            .ifPresent(existing -> { throw new DuplicateEmailException(cmd.email()); });

        employee.updateProfile(cmd.name(), cmd.email(), role);
        return repository.save(employee);
    }
}

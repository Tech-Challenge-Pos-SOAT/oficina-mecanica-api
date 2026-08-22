package com.postech.oficinamecanica.application.employee;

import com.postech.oficinamecanica.domain.employee.DuplicateEmailException;
import com.postech.oficinamecanica.domain.employee.Employee;
import com.postech.oficinamecanica.domain.employee.EmployeeRole;
import org.springframework.stereotype.Service;

@Service
public class CreateEmployeeUseCase {
    private final EmployeeRepository repository;
    private final PasswordEncoder passwordEncoder;

    public CreateEmployeeUseCase(EmployeeRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    public Employee execute(CreateEmployeeCommand cmd) {
        EmployeeRole role = EmployeeRole.fromValue(cmd.role());

        repository.findByEmail(cmd.email())
            .ifPresent(existing -> { throw new DuplicateEmailException(cmd.email()); });

        String passwordHash = passwordEncoder.encode(cmd.password());
        Employee employee = Employee.create(cmd.name(), cmd.email(), passwordHash, role);
        return repository.save(employee);
    }
}

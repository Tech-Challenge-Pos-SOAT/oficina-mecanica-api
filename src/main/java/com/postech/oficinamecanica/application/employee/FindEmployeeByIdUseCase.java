package com.postech.oficinamecanica.application.employee;

import com.postech.oficinamecanica.domain.employee.Employee;
import com.postech.oficinamecanica.domain.employee.EmployeeNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class FindEmployeeByIdUseCase {
    private final EmployeeRepository repository;

    public FindEmployeeByIdUseCase(EmployeeRepository repository) {
        this.repository = repository;
    }

    public Employee execute(Long id) {
        return repository.findById(id)
            .orElseThrow(() -> new EmployeeNotFoundException(id));
    }
}

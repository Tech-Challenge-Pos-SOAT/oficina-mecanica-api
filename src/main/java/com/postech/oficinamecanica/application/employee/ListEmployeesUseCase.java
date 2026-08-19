package com.postech.oficinamecanica.application.employee;

import com.postech.oficinamecanica.domain.employee.Employee;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ListEmployeesUseCase {
    private final EmployeeRepository repository;

    public ListEmployeesUseCase(EmployeeRepository repository) {
        this.repository = repository;
    }

    public List<Employee> execute() {
        return repository.findAll();
    }
}

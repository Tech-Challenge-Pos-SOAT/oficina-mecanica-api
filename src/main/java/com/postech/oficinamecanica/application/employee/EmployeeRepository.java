package com.postech.oficinamecanica.application.employee;

import com.postech.oficinamecanica.domain.employee.Employee;
import java.util.List;
import java.util.Optional;

public interface EmployeeRepository {
    Employee save(Employee employee);
    Optional<Employee> findById(Long id);
    Optional<Employee> findByEmail(String email);
    List<Employee> findAll();
}

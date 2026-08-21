package com.postech.oficinamecanica.domain.employee;

public class EmployeeNotFoundException extends RuntimeException {
    public EmployeeNotFoundException(Long id) {
        super("Employee not found: " + id);
    }
}

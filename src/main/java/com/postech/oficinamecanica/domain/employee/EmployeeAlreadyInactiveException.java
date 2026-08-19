package com.postech.oficinamecanica.domain.employee;

public class EmployeeAlreadyInactiveException extends RuntimeException {
    public EmployeeAlreadyInactiveException(Long id) {
        super("Employee already inactive: " + id);
    }
}

package com.postech.oficinamecanica.domain.employee;

public class EmployeeAlreadyActiveException extends RuntimeException {
    public EmployeeAlreadyActiveException(Long id) {
        super("Employee already active: " + id);
    }
}

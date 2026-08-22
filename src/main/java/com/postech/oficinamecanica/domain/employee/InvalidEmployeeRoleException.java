package com.postech.oficinamecanica.domain.employee;

public class InvalidEmployeeRoleException extends RuntimeException {
    public InvalidEmployeeRoleException(String role) {
        super("Invalid employee role: " + role);
    }
}

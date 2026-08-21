package com.postech.oficinamecanica.domain.employee;

public enum EmployeeRole {
    MECHANIC, ATTENDANT;

    public static EmployeeRole fromValue(String value) {
        try {
            return EmployeeRole.valueOf(value);
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new InvalidEmployeeRoleException(value);
        }
    }
}

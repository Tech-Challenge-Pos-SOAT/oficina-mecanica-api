package com.postech.oficinamecanica.application.auth;

import com.postech.oficinamecanica.domain.employee.Employee;

public interface TokenProvider {
    String generateToken(Employee employee);

    boolean isValid(String token);
}

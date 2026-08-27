package com.postech.oficinamecanica.application.auth;

import com.postech.oficinamecanica.domain.employee.Employee;

public record LoginResult(String token, Employee employee) {}

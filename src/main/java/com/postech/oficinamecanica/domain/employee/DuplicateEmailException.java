package com.postech.oficinamecanica.domain.employee;

public class DuplicateEmailException extends RuntimeException {
    public DuplicateEmailException(String email) {
        super("Email already in use: " + email);
    }
}

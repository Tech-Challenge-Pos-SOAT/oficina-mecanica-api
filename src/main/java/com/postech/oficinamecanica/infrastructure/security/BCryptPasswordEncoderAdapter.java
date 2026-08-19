package com.postech.oficinamecanica.infrastructure.security;

import com.postech.oficinamecanica.application.employee.PasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class BCryptPasswordEncoderAdapter implements PasswordEncoder {
    private final BCryptPasswordEncoder delegate = new BCryptPasswordEncoder();

    @Override
    public String encode(String rawPassword) {
        return delegate.encode(rawPassword);
    }
}

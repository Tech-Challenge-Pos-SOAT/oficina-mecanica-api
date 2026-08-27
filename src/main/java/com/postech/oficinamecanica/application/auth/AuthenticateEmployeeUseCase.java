package com.postech.oficinamecanica.application.auth;

import com.postech.oficinamecanica.application.employee.EmployeeRepository;
import com.postech.oficinamecanica.application.employee.PasswordEncoder;
import com.postech.oficinamecanica.domain.auth.InvalidCredentialsException;
import com.postech.oficinamecanica.domain.employee.Employee;
import com.postech.oficinamecanica.domain.shared.EntityStatus;
import org.springframework.stereotype.Service;

@Service
public class AuthenticateEmployeeUseCase {
    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenProvider tokenProvider;

    public AuthenticateEmployeeUseCase(EmployeeRepository employeeRepository,
                                        PasswordEncoder passwordEncoder,
                                        TokenProvider tokenProvider) {
        this.employeeRepository = employeeRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
    }

    public LoginResult execute(LoginCommand command) {
        Employee employee = employeeRepository.findByEmail(command.email())
            .orElseThrow(InvalidCredentialsException::new);

        if (employee.getStatus() != EntityStatus.ACTIVE) {
            throw new InvalidCredentialsException();
        }

        if (!passwordEncoder.matches(command.password(), employee.getPassword())) {
            throw new InvalidCredentialsException();
        }

        String token = tokenProvider.generateToken(employee);
        return new LoginResult(token, employee);
    }
}

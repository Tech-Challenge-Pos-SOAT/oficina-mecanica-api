package com.postech.oficinamecanica.application.auth;

import com.postech.oficinamecanica.application.employee.EmployeeRepository;
import com.postech.oficinamecanica.application.employee.PasswordEncoder;
import com.postech.oficinamecanica.domain.auth.InvalidCredentialsException;
import com.postech.oficinamecanica.domain.employee.Employee;
import com.postech.oficinamecanica.domain.employee.EmployeeRole;
import com.postech.oficinamecanica.domain.shared.EntityStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticateEmployeeUseCaseTest {
    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private TokenProvider tokenProvider;

    @InjectMocks
    private AuthenticateEmployeeUseCase useCase;

    @Test
    void shouldReturnTokenWhenCredentialsAreValid() {
        Employee employee = anEmployee(EntityStatus.ACTIVE);
        LoginCommand command = new LoginCommand("carlos.souza@oficina.com", "senha123");

        when(employeeRepository.findByEmail("carlos.souza@oficina.com")).thenReturn(Optional.of(employee));
        when(passwordEncoder.matches("senha123", "hashed-password")).thenReturn(true);
        when(tokenProvider.generateToken(employee)).thenReturn("a-jwt-token");

        LoginResult result = useCase.execute(command);

        assertThat(result.token()).isEqualTo("a-jwt-token");
        assertThat(result.employee()).isEqualTo(employee);
    }

    @Test
    void shouldThrowInvalidCredentialsWhenEmailDoesNotExist() {
        LoginCommand command = new LoginCommand("inexistente@oficina.com", "senha123");

        when(employeeRepository.findByEmail("inexistente@oficina.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(command))
            .isInstanceOf(InvalidCredentialsException.class)
            .hasMessage("Credenciais inválidas");

        verify(tokenProvider, never()).generateToken(any());
    }

    @Test
    void shouldThrowInvalidCredentialsWhenEmployeeIsInactive() {
        Employee employee = anEmployee(EntityStatus.INACTIVE);
        LoginCommand command = new LoginCommand("carlos.souza@oficina.com", "senha123");

        when(employeeRepository.findByEmail("carlos.souza@oficina.com")).thenReturn(Optional.of(employee));

        assertThatThrownBy(() -> useCase.execute(command))
            .isInstanceOf(InvalidCredentialsException.class)
            .hasMessage("Credenciais inválidas");

        verify(tokenProvider, never()).generateToken(any());
    }

    @Test
    void shouldThrowInvalidCredentialsWhenPasswordDoesNotMatch() {
        Employee employee = anEmployee(EntityStatus.ACTIVE);
        LoginCommand command = new LoginCommand("carlos.souza@oficina.com", "senha-errada");

        when(employeeRepository.findByEmail("carlos.souza@oficina.com")).thenReturn(Optional.of(employee));
        when(passwordEncoder.matches("senha-errada", "hashed-password")).thenReturn(false);

        assertThatThrownBy(() -> useCase.execute(command))
            .isInstanceOf(InvalidCredentialsException.class)
            .hasMessage("Credenciais inválidas");

        verify(tokenProvider, never()).generateToken(any());
    }

    private static Employee anEmployee(EntityStatus status) {
        Instant now = Instant.now();
        return new Employee(1L, "Carlos Souza", "carlos.souza@oficina.com", "hashed-password",
            EmployeeRole.ATTENDANT, status, now, now);
    }
}

package com.postech.oficinamecanica.application.employee;

import com.postech.oficinamecanica.domain.employee.DuplicateEmailException;
import com.postech.oficinamecanica.domain.employee.Employee;
import com.postech.oficinamecanica.domain.employee.EmployeeNotFoundException;
import com.postech.oficinamecanica.domain.employee.EmployeeRole;
import com.postech.oficinamecanica.domain.employee.InvalidEmployeeRoleException;
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
class UpdateEmployeeUseCaseTest {
    @Mock
    private EmployeeRepository repository;

    @InjectMocks
    private UpdateEmployeeUseCase useCase;

    private Employee anEmployee(Long id, String name, String email, EmployeeRole role) {
        return new Employee(id, name, email, "hash", role, EntityStatus.ACTIVE, Instant.now(), Instant.now());
    }

    @Test
    void shouldUpdateEmployeeProfile() {
        Employee existing = anEmployee(1L, "Carlos Souza", "carlos.souza@oficina.com", EmployeeRole.MECHANIC);
        UpdateEmployeeCommand cmd = new UpdateEmployeeCommand(1L, "Carlos S. Souza", "carlos.novo@oficina.com", "ATTENDANT");

        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.findByEmail("carlos.novo@oficina.com")).thenReturn(Optional.empty());
        when(repository.save(any(Employee.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Employee result = useCase.execute(cmd);

        assertThat(result.getName()).isEqualTo("Carlos S. Souza");
        assertThat(result.getEmail()).isEqualTo("carlos.novo@oficina.com");
        assertThat(result.getRole()).isEqualTo(EmployeeRole.ATTENDANT);
    }

    @Test
    void shouldAllowKeepingSameEmailOnUpdate() {
        Employee existing = anEmployee(1L, "Carlos Souza", "carlos.souza@oficina.com", EmployeeRole.MECHANIC);
        UpdateEmployeeCommand cmd = new UpdateEmployeeCommand(1L, "Carlos Souza", "carlos.souza@oficina.com", "MECHANIC");

        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.findByEmail("carlos.souza@oficina.com")).thenReturn(Optional.of(existing));
        when(repository.save(any(Employee.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Employee result = useCase.execute(cmd);

        assertThat(result.getEmail()).isEqualTo("carlos.souza@oficina.com");
    }

    @Test
    void shouldFailWhenEmployeeNotFound() {
        UpdateEmployeeCommand cmd = new UpdateEmployeeCommand(99L, "Carlos Souza", "carlos.souza@oficina.com", "MECHANIC");

        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(cmd))
            .isInstanceOf(EmployeeNotFoundException.class);

        verify(repository, never()).save(any());
    }

    @Test
    void shouldFailWhenEmailBelongsToAnotherEmployee() {
        Employee existing = anEmployee(1L, "Carlos Souza", "carlos.souza@oficina.com", EmployeeRole.MECHANIC);
        Employee otherEmployee = anEmployee(2L, "Ana Lima", "ana.lima@oficina.com", EmployeeRole.ATTENDANT);
        UpdateEmployeeCommand cmd = new UpdateEmployeeCommand(1L, "Carlos Souza", "ana.lima@oficina.com", "MECHANIC");

        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.findByEmail("ana.lima@oficina.com")).thenReturn(Optional.of(otherEmployee));

        assertThatThrownBy(() -> useCase.execute(cmd))
            .isInstanceOf(DuplicateEmailException.class);

        verify(repository, never()).save(any());
    }

    @Test
    void shouldFailWhenRoleIsInvalid() {
        Employee existing = anEmployee(1L, "Carlos Souza", "carlos.souza@oficina.com", EmployeeRole.MECHANIC);
        UpdateEmployeeCommand cmd = new UpdateEmployeeCommand(1L, "Carlos Souza", "carlos.souza@oficina.com", "OWNER");

        when(repository.findById(1L)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> useCase.execute(cmd))
            .isInstanceOf(InvalidEmployeeRoleException.class);

        verify(repository, never()).save(any());
    }
}

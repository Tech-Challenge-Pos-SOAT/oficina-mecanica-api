package com.postech.oficinamecanica.application.employee;

import com.postech.oficinamecanica.domain.employee.Employee;
import com.postech.oficinamecanica.domain.employee.EmployeeAlreadyActiveException;
import com.postech.oficinamecanica.domain.employee.EmployeeAlreadyInactiveException;
import com.postech.oficinamecanica.domain.employee.EmployeeNotFoundException;
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
class UpdateEmployeeStatusUseCaseTest {
    @Mock
    private EmployeeRepository repository;

    @InjectMocks
    private UpdateEmployeeStatusUseCase useCase;

    private Employee anEmployee(EntityStatus status) {
        return new Employee(1L, "Carlos Souza", "carlos.souza@oficina.com", "hash", EmployeeRole.MECHANIC, status, Instant.now(), Instant.now());
    }

    @Test
    void shouldDeactivateActiveEmployee() {
        Employee existing = anEmployee(EntityStatus.ACTIVE);

        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.save(any(Employee.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Employee result = useCase.execute(1L, "INACTIVE");

        assertThat(result.getStatus()).isEqualTo(EntityStatus.INACTIVE);
    }

    @Test
    void shouldActivateInactiveEmployee() {
        Employee existing = anEmployee(EntityStatus.INACTIVE);

        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.save(any(Employee.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Employee result = useCase.execute(1L, "active");

        assertThat(result.getStatus()).isEqualTo(EntityStatus.ACTIVE);
    }

    @Test
    void shouldFailWhenReactivatingAlreadyActiveEmployee() {
        Employee existing = anEmployee(EntityStatus.ACTIVE);

        when(repository.findById(1L)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> useCase.execute(1L, "ACTIVE"))
            .isInstanceOf(EmployeeAlreadyActiveException.class);

        verify(repository, never()).save(any());
    }

    @Test
    void shouldFailWhenDeactivatingAlreadyInactiveEmployee() {
        Employee existing = anEmployee(EntityStatus.INACTIVE);

        when(repository.findById(1L)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> useCase.execute(1L, "INACTIVE"))
            .isInstanceOf(EmployeeAlreadyInactiveException.class);

        verify(repository, never()).save(any());
    }

    @Test
    void shouldFailWhenEmployeeNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(99L, "ACTIVE"))
            .isInstanceOf(EmployeeNotFoundException.class);

        verify(repository, never()).save(any());
    }

    @Test
    void shouldFailWhenStatusValueIsInvalid() {
        Employee existing = anEmployee(EntityStatus.ACTIVE);

        when(repository.findById(1L)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> useCase.execute(1L, "SUSPENDED"))
            .isInstanceOf(IllegalArgumentException.class);

        verify(repository, never()).save(any());
    }
}

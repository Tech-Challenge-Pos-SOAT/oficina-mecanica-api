package com.postech.oficinamecanica.application.employee;

import com.postech.oficinamecanica.domain.employee.Employee;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FindEmployeeByIdUseCaseTest {
    @Mock
    private EmployeeRepository repository;

    @InjectMocks
    private FindEmployeeByIdUseCase useCase;

    @Test
    void shouldReturnEmployeeWhenFound() {
        Employee employee = new Employee(1L, "Carlos Souza", "carlos.souza@oficina.com", "hash", EmployeeRole.MECHANIC, EntityStatus.ACTIVE, Instant.now(), Instant.now());

        when(repository.findById(1L)).thenReturn(Optional.of(employee));

        Employee result = useCase.execute(1L);

        assertThat(result.getName()).isEqualTo("Carlos Souza");
    }

    @Test
    void shouldFailWhenEmployeeNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(99L))
            .isInstanceOf(EmployeeNotFoundException.class);
    }
}

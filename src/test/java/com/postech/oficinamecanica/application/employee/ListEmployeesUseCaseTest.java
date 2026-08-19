package com.postech.oficinamecanica.application.employee;

import com.postech.oficinamecanica.domain.employee.Employee;
import com.postech.oficinamecanica.domain.employee.EmployeeRole;
import com.postech.oficinamecanica.domain.shared.EntityStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListEmployeesUseCaseTest {
    @Mock
    private EmployeeRepository repository;

    @InjectMocks
    private ListEmployeesUseCase useCase;

    @Test
    void shouldReturnAllEmployees() {
        Employee active = new Employee(1L, "Carlos Souza", "carlos.souza@oficina.com", "hash", EmployeeRole.MECHANIC, EntityStatus.ACTIVE, Instant.now(), Instant.now());
        Employee inactive = new Employee(2L, "Ana Lima", "ana.lima@oficina.com", "hash2", EmployeeRole.ATTENDANT, EntityStatus.INACTIVE, Instant.now(), Instant.now());

        when(repository.findAll()).thenReturn(List.of(active, inactive));

        List<Employee> result = useCase.execute();

        assertThat(result).hasSize(2);
    }

    @Test
    void shouldReturnEmptyListWhenNoEmployeesExist() {
        when(repository.findAll()).thenReturn(List.of());

        List<Employee> result = useCase.execute();

        assertThat(result).isEmpty();
    }
}

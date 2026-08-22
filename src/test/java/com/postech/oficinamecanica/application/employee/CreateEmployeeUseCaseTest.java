package com.postech.oficinamecanica.application.employee;

import com.postech.oficinamecanica.domain.employee.DuplicateEmailException;
import com.postech.oficinamecanica.domain.employee.Employee;
import com.postech.oficinamecanica.domain.employee.EmployeeRole;
import com.postech.oficinamecanica.domain.employee.InvalidEmployeeRoleException;
import com.postech.oficinamecanica.domain.shared.EntityStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateEmployeeUseCaseTest {
    @Mock
    private EmployeeRepository repository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private CreateEmployeeUseCase useCase;

    @Test
    void shouldCreateEmployeeWhenEmailIsUnique() {
        CreateEmployeeCommand cmd = new CreateEmployeeCommand("Carlos Souza", "carlos.souza@oficina.com", "senha123", "MECHANIC");

        when(repository.findByEmail("carlos.souza@oficina.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("senha123")).thenReturn("hashed-password");
        when(repository.save(any(Employee.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Employee result = useCase.execute(cmd);

        assertThat(result.getName()).isEqualTo("Carlos Souza");
        assertThat(result.getEmail()).isEqualTo("carlos.souza@oficina.com");
        assertThat(result.getPassword()).isEqualTo("hashed-password");
        assertThat(result.getRole()).isEqualTo(EmployeeRole.MECHANIC);
        assertThat(result.getStatus()).isEqualTo(EntityStatus.ACTIVE);
    }

    @Test
    void shouldStorePasswordAsHashNeverAsRawValue() {
        CreateEmployeeCommand cmd = new CreateEmployeeCommand("Carlos Souza", "carlos.souza@oficina.com", "senha123", "MECHANIC");

        when(repository.findByEmail("carlos.souza@oficina.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("senha123")).thenReturn("hashed-password");
        ArgumentCaptor<Employee> captor = ArgumentCaptor.forClass(Employee.class);
        when(repository.save(captor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        useCase.execute(cmd);

        assertThat(captor.getValue().getPassword()).isEqualTo("hashed-password").isNotEqualTo("senha123");
    }

    @Test
    void shouldFailWhenEmailAlreadyExists() {
        CreateEmployeeCommand cmd = new CreateEmployeeCommand("Carlos Souza", "carlos.souza@oficina.com", "senha123", "MECHANIC");
        Employee existing = Employee.create("Outro Nome", "carlos.souza@oficina.com", "outro-hash", EmployeeRole.ATTENDANT);

        when(repository.findByEmail("carlos.souza@oficina.com")).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> useCase.execute(cmd))
            .isInstanceOf(DuplicateEmailException.class);

        verify(repository, never()).save(any());
    }

    @Test
    void shouldFailWhenRoleIsInvalid() {
        CreateEmployeeCommand cmd = new CreateEmployeeCommand("Carlos Souza", "carlos.souza@oficina.com", "senha123", "OWNER");

        assertThatThrownBy(() -> useCase.execute(cmd))
            .isInstanceOf(InvalidEmployeeRoleException.class);

        verify(repository, never()).save(any());
    }
}

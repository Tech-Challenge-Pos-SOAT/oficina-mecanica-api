package com.postech.oficinamecanica.interfaces.rest.employee;

import com.postech.oficinamecanica.application.employee.CreateEmployeeCommand;
import com.postech.oficinamecanica.application.employee.UpdateEmployeeCommand;
import com.postech.oficinamecanica.domain.employee.Employee;
import com.postech.oficinamecanica.domain.employee.EmployeeRole;
import com.postech.oficinamecanica.domain.shared.EntityStatus;
import org.junit.jupiter.api.Test;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class EmployeeRestMapperTest {
    private final EmployeeRestMapper mapper = new EmployeeRestMapperImpl();

    @Test
    void shouldMapEmployeeToResponse() {
        Instant now = Instant.now();
        Employee employee = new Employee(
            1L, "Carlos Souza", "carlos.souza@oficina.com", "hashed-password",
            EmployeeRole.MECHANIC, EntityStatus.ACTIVE, now, now
        );

        EmployeeResponse response = mapper.toResponse(employee);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("Carlos Souza");
        assertThat(response.email()).isEqualTo("carlos.souza@oficina.com");
        assertThat(response.role()).isEqualTo("MECHANIC");
        assertThat(response.status()).isEqualTo("ACTIVE");
    }

    @Test
    void shouldMapCreateRequestToCommand() {
        CreateEmployeeRequest request = new CreateEmployeeRequest(
            "Carlos Souza", "carlos.souza@oficina.com", "senha123", "MECHANIC"
        );

        CreateEmployeeCommand cmd = mapper.toCommand(request);

        assertThat(cmd.name()).isEqualTo("Carlos Souza");
        assertThat(cmd.email()).isEqualTo("carlos.souza@oficina.com");
        assertThat(cmd.password()).isEqualTo("senha123");
        assertThat(cmd.role()).isEqualTo("MECHANIC");
    }

    @Test
    void shouldMapUpdateRequestToCommandWithId() {
        UpdateEmployeeRequest request = new UpdateEmployeeRequest("Carlos Lima", "carlos.lima@oficina.com", "ATTENDANT");

        UpdateEmployeeCommand cmd = mapper.toCommand(1L, request);

        assertThat(cmd.id()).isEqualTo(1L);
        assertThat(cmd.name()).isEqualTo("Carlos Lima");
        assertThat(cmd.email()).isEqualTo("carlos.lima@oficina.com");
        assertThat(cmd.role()).isEqualTo("ATTENDANT");
    }
}

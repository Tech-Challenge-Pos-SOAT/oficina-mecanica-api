package com.postech.oficinamecanica.domain.employee;

import com.postech.oficinamecanica.domain.shared.EntityStatus;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EmployeeTest {

    @Test
    void shouldCreateEmployeeWithActiveStatus() {
        Employee employee = Employee.create("Carlos Souza", "carlos.souza@oficina.com", "hashed-password", EmployeeRole.MECHANIC);

        assertThat(employee.getStatus()).isEqualTo(EntityStatus.ACTIVE);
        assertThat(employee.getName()).isEqualTo("Carlos Souza");
        assertThat(employee.getEmail()).isEqualTo("carlos.souza@oficina.com");
        assertThat(employee.getPassword()).isEqualTo("hashed-password");
        assertThat(employee.getRole()).isEqualTo(EmployeeRole.MECHANIC);
    }

    @Test
    void shouldUpdateProfileFields() {
        Employee employee = Employee.create("Carlos Souza", "carlos.souza@oficina.com", "hashed-password", EmployeeRole.MECHANIC);

        employee.updateProfile("Carlos S. Souza", "carlos.novo@oficina.com", EmployeeRole.ATTENDANT);

        assertThat(employee.getName()).isEqualTo("Carlos S. Souza");
        assertThat(employee.getEmail()).isEqualTo("carlos.novo@oficina.com");
        assertThat(employee.getRole()).isEqualTo(EmployeeRole.ATTENDANT);
    }

    @Test
    void shouldNotAllowReactivatingAlreadyActiveEmployee() {
        Employee employee = Employee.create("Carlos Souza", "carlos.souza@oficina.com", "hashed-password", EmployeeRole.MECHANIC);

        assertThatThrownBy(employee::activate)
            .isInstanceOf(EmployeeAlreadyActiveException.class);
    }

    @Test
    void shouldNotAllowDeactivatingAlreadyInactiveEmployee() {
        Employee employee = Employee.create("Carlos Souza", "carlos.souza@oficina.com", "hashed-password", EmployeeRole.MECHANIC);
        employee.deactivate();

        assertThatThrownBy(employee::deactivate)
            .isInstanceOf(EmployeeAlreadyInactiveException.class);
    }

    @Test
    void shouldActivateInactiveEmployee() {
        Employee employee = Employee.create("Carlos Souza", "carlos.souza@oficina.com", "hashed-password", EmployeeRole.MECHANIC);
        employee.deactivate();

        employee.activate();

        assertThat(employee.getStatus()).isEqualTo(EntityStatus.ACTIVE);
    }
}

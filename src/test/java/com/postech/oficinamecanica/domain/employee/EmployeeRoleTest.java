package com.postech.oficinamecanica.domain.employee;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EmployeeRoleTest {

    @ParameterizedTest
    @ValueSource(strings = {"MECHANIC", "ATTENDANT"})
    void shouldResolveValidRoles(String value) {
        assertThat(EmployeeRole.fromValue(value)).isEqualTo(EmployeeRole.valueOf(value));
    }

    @ParameterizedTest
    @ValueSource(strings = {"ADMIN", "mechanic", "", "OWNER"})
    void shouldRejectInvalidRole(String value) {
        assertThatThrownBy(() -> EmployeeRole.fromValue(value))
            .isInstanceOf(InvalidEmployeeRoleException.class);
    }

    @Test
    void shouldRejectNullRole() {
        assertThatThrownBy(() -> EmployeeRole.fromValue(null))
            .isInstanceOf(InvalidEmployeeRoleException.class);
    }
}

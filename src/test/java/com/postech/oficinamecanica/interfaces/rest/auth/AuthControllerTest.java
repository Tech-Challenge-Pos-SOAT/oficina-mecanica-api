package com.postech.oficinamecanica.interfaces.rest.auth;

import com.postech.oficinamecanica.application.auth.AuthenticateEmployeeUseCase;
import com.postech.oficinamecanica.application.auth.LoginResult;
import com.postech.oficinamecanica.domain.auth.InvalidCredentialsException;
import com.postech.oficinamecanica.domain.employee.Employee;
import com.postech.oficinamecanica.domain.employee.EmployeeRole;
import com.postech.oficinamecanica.domain.shared.EntityStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
class AuthControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthenticateEmployeeUseCase authenticateEmployeeUseCase;

    @Test
    void shouldReturnTokenWhenCredentialsAreValid() throws Exception {
        Employee employee = anEmployee();
        when(authenticateEmployeeUseCase.execute(any())).thenReturn(new LoginResult("a-jwt-token", employee));

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\": \"carlos.souza@oficina.com\", \"password\": \"senha123\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").value("a-jwt-token"))
            .andExpect(jsonPath("$.type").value("Bearer"))
            .andExpect(jsonPath("$.employeeId").value(1))
            .andExpect(jsonPath("$.name").value("Carlos Souza"))
            .andExpect(jsonPath("$.role").value("ATTENDANT"));
    }

    @Test
    void shouldReturnUnauthorizedWithGenericMessageWhenCredentialsAreInvalid() throws Exception {
        when(authenticateEmployeeUseCase.execute(any())).thenThrow(new InvalidCredentialsException());

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\": \"carlos.souza@oficina.com\", \"password\": \"errada\"}"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.message").value("Credenciais inválidas"))
            .andExpect(jsonPath("$.code").doesNotExist());
    }

    @Test
    void shouldReturnBadRequestWhenEmailIsBlank() throws Exception {
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\": \"\", \"password\": \"senha123\"}"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenPasswordIsBlank() throws Exception {
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\": \"carlos.souza@oficina.com\", \"password\": \"\"}"))
            .andExpect(status().isBadRequest());
    }

    private static Employee anEmployee() {
        Instant now = Instant.now();
        return new Employee(1L, "Carlos Souza", "carlos.souza@oficina.com", "hashed-password",
            EmployeeRole.ATTENDANT, EntityStatus.ACTIVE, now, now);
    }
}

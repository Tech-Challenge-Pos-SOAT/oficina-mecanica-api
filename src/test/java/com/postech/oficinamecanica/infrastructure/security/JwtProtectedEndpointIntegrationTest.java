package com.postech.oficinamecanica.infrastructure.security;

import com.postech.oficinamecanica.application.auth.TokenProvider;
import com.postech.oficinamecanica.application.employee.CreateEmployeeUseCase;
import com.postech.oficinamecanica.application.employee.FindEmployeeByIdUseCase;
import com.postech.oficinamecanica.application.employee.ListEmployeesUseCase;
import com.postech.oficinamecanica.application.employee.UpdateEmployeeStatusUseCase;
import com.postech.oficinamecanica.application.employee.UpdateEmployeeUseCase;
import com.postech.oficinamecanica.interfaces.rest.employee.EmployeeController;
import com.postech.oficinamecanica.interfaces.rest.employee.EmployeeRestMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Verifica ponta a ponta que o {@link JwtAuthenticationFilter}, registrado via
 * {@link JwtFilterConfig}, realmente protege uma rota administrativa
 * ("/api/employees" é usada aqui só como representante). Usa @Import para trazer
 * o filtro pra dentro desta fatia @WebMvcTest especificamente — os testes
 * @WebMvcTest já existentes de outros integrantes (Customer/Material/Service/
 * Vehicle) não importam essa config e continuam sem exigir token.
 */
@WebMvcTest(EmployeeController.class)
@Import(JwtFilterConfig.class)
class JwtProtectedEndpointIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TokenProvider tokenProvider;

    @MockitoBean
    private CreateEmployeeUseCase createEmployeeUseCase;

    @MockitoBean
    private UpdateEmployeeUseCase updateEmployeeUseCase;

    @MockitoBean
    private UpdateEmployeeStatusUseCase updateEmployeeStatusUseCase;

    @MockitoBean
    private FindEmployeeByIdUseCase findEmployeeByIdUseCase;

    @MockitoBean
    private ListEmployeesUseCase listEmployeesUseCase;

    @MockitoBean
    private EmployeeRestMapper mapper;

    @Test
    void shouldRejectRequestWithoutToken() throws Exception {
        mockMvc.perform(get("/api/employees"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void shouldRejectRequestWithMalformedToken() throws Exception {
        when(tokenProvider.isValid("token-malformado")).thenReturn(false);

        mockMvc.perform(get("/api/employees").header("Authorization", "Bearer token-malformado"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldAllowRequestWithValidToken() throws Exception {
        when(tokenProvider.isValid("token-valido")).thenReturn(true);
        when(listEmployeesUseCase.execute()).thenReturn(List.of());

        mockMvc.perform(get("/api/employees").header("Authorization", "Bearer token-valido"))
            .andExpect(status().isOk());
    }
}

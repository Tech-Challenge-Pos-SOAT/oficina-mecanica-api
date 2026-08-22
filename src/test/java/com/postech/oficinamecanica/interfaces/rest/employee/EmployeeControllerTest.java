package com.postech.oficinamecanica.interfaces.rest.employee;

import com.postech.oficinamecanica.application.employee.CreateEmployeeCommand;
import com.postech.oficinamecanica.application.employee.CreateEmployeeUseCase;
import com.postech.oficinamecanica.application.employee.FindEmployeeByIdUseCase;
import com.postech.oficinamecanica.application.employee.ListEmployeesUseCase;
import com.postech.oficinamecanica.application.employee.UpdateEmployeeCommand;
import com.postech.oficinamecanica.application.employee.UpdateEmployeeStatusUseCase;
import com.postech.oficinamecanica.application.employee.UpdateEmployeeUseCase;
import com.postech.oficinamecanica.domain.employee.DuplicateEmailException;
import com.postech.oficinamecanica.domain.employee.Employee;
import com.postech.oficinamecanica.domain.employee.EmployeeAlreadyActiveException;
import com.postech.oficinamecanica.domain.employee.EmployeeNotFoundException;
import com.postech.oficinamecanica.domain.employee.EmployeeRole;
import com.postech.oficinamecanica.domain.employee.InvalidEmployeeRoleException;
import com.postech.oficinamecanica.domain.shared.EntityStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EmployeeController.class)
class EmployeeControllerTest {
    @Autowired
    private MockMvc mockMvc;

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

    private Employee anEmployee() {
        return new Employee(1L, "Carlos Souza", "carlos.souza@oficina.com", "hashed-password",
            EmployeeRole.MECHANIC, EntityStatus.ACTIVE, Instant.now(), Instant.now());
    }

    private EmployeeResponse aResponse(Employee employee) {
        return new EmployeeResponse(
            employee.getId(), employee.getName(), employee.getEmail(), employee.getRole().name(),
            employee.getStatus().name(), employee.getCreatedAt(), employee.getUpdatedAt()
        );
    }

    private CreateEmployeeCommand aCreateCommand() {
        return new CreateEmployeeCommand("Carlos Souza", "carlos.souza@oficina.com", "senha123", "MECHANIC");
    }

    private UpdateEmployeeCommand anUpdateCommand() {
        return new UpdateEmployeeCommand(1L, "Carlos Souza", "carlos.souza@oficina.com", "ATTENDANT");
    }

    @Test
    void shouldCreateEmployeeAndReturn201() throws Exception {
        Employee employee = anEmployee();
        EmployeeResponse response = aResponse(employee);

        when(mapper.toCommand(any(CreateEmployeeRequest.class))).thenReturn(aCreateCommand());
        when(createEmployeeUseCase.execute(any())).thenReturn(employee);
        when(mapper.toResponse(employee)).thenReturn(response);

        String body = """
            {"name":"Carlos Souza","email":"carlos.souza@oficina.com","password":"senha123","role":"MECHANIC"}
            """;

        mockMvc.perform(post("/api/employees").contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.name").value("Carlos Souza"))
            .andExpect(jsonPath("$.email").value("carlos.souza@oficina.com"))
            .andExpect(jsonPath("$.role").value("MECHANIC"))
            .andExpect(jsonPath("$.status").value("ACTIVE"))
            .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void shouldReturn400WhenRoleIsInvalidOnCreate() throws Exception {
        when(mapper.toCommand(any(CreateEmployeeRequest.class))).thenReturn(aCreateCommand());
        when(createEmployeeUseCase.execute(any())).thenThrow(new InvalidEmployeeRoleException("OWNER"));

        String body = """
            {"name":"Carlos Souza","email":"carlos.souza@oficina.com","password":"senha123","role":"OWNER"}
            """;

        mockMvc.perform(post("/api/employees").contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("INVALID_ROLE"));
    }

    @Test
    void shouldReturn409WhenEmailIsDuplicateOnCreate() throws Exception {
        when(mapper.toCommand(any(CreateEmployeeRequest.class))).thenReturn(aCreateCommand());
        when(createEmployeeUseCase.execute(any())).thenThrow(new DuplicateEmailException("carlos.souza@oficina.com"));

        String body = """
            {"name":"Carlos Souza","email":"carlos.souza@oficina.com","password":"senha123","role":"MECHANIC"}
            """;

        mockMvc.perform(post("/api/employees").contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.code").value("DUPLICATE_EMAIL"));
    }

    @Test
    void shouldReturn400WhenRequiredFieldIsBlankOnCreate() throws Exception {
        String body = """
            {"name":"","email":"carlos.souza@oficina.com","password":"senha123","role":"MECHANIC"}
            """;

        mockMvc.perform(post("/api/employees").contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isBadRequest());
    }

    @Test
    void shouldUpdateEmployeeAndReturn200() throws Exception {
        Employee employee = anEmployee();
        EmployeeResponse response = aResponse(employee);

        when(mapper.toCommand(eq(1L), any(UpdateEmployeeRequest.class))).thenReturn(anUpdateCommand());
        when(updateEmployeeUseCase.execute(any())).thenReturn(employee);
        when(mapper.toResponse(employee)).thenReturn(response);

        String body = """
            {"name":"Carlos Souza","email":"carlos.souza@oficina.com","role":"ATTENDANT"}
            """;

        mockMvc.perform(put("/api/employees/1").contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Carlos Souza"))
            .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void shouldReturn404WhenEmployeeNotFoundOnUpdate() throws Exception {
        when(mapper.toCommand(eq(1L), any(UpdateEmployeeRequest.class))).thenReturn(anUpdateCommand());
        when(updateEmployeeUseCase.execute(any())).thenThrow(new EmployeeNotFoundException(1L));

        String body = """
            {"name":"Carlos Souza","email":"carlos.souza@oficina.com","role":"ATTENDANT"}
            """;

        mockMvc.perform(put("/api/employees/1").contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value("EMPLOYEE_NOT_FOUND"));
    }

    @Test
    void shouldReturnEmployeeByIdAndReturn200() throws Exception {
        Employee employee = anEmployee();
        EmployeeResponse response = aResponse(employee);

        when(findEmployeeByIdUseCase.execute(1L)).thenReturn(employee);
        when(mapper.toResponse(employee)).thenReturn(response);

        mockMvc.perform(get("/api/employees/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void shouldReturn404WhenEmployeeNotFoundOnGet() throws Exception {
        when(findEmployeeByIdUseCase.execute(99L)).thenThrow(new EmployeeNotFoundException(99L));

        mockMvc.perform(get("/api/employees/99"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value("EMPLOYEE_NOT_FOUND"));
    }

    @Test
    void shouldListAllEmployeesAndReturn200() throws Exception {
        Employee employee = anEmployee();
        EmployeeResponse response = aResponse(employee);

        when(listEmployeesUseCase.execute()).thenReturn(List.of(employee));
        when(mapper.toResponse(employee)).thenReturn(response);

        mockMvc.perform(get("/api/employees"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].name").value("Carlos Souza"));
    }

    @Test
    void shouldReturnEmptyListWhenNoEmployeesExist() throws Exception {
        when(listEmployeesUseCase.execute()).thenReturn(List.of());

        mockMvc.perform(get("/api/employees"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void shouldUpdateStatusAndReturn200() throws Exception {
        Employee employee = anEmployee();
        EmployeeResponse response = aResponse(employee);

        when(updateEmployeeStatusUseCase.execute(eq(1L), eq("INACTIVE"))).thenReturn(employee);
        when(mapper.toResponse(employee)).thenReturn(response);

        String body = """
            {"status":"INACTIVE"}
            """;

        mockMvc.perform(patch("/api/employees/1/status").contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isOk());
    }

    @Test
    void shouldReturn409WhenReactivatingAlreadyActiveEmployee() throws Exception {
        when(updateEmployeeStatusUseCase.execute(eq(1L), eq("ACTIVE")))
            .thenThrow(new EmployeeAlreadyActiveException(1L));

        String body = """
            {"status":"ACTIVE"}
            """;

        mockMvc.perform(patch("/api/employees/1/status").contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.code").value("EMPLOYEE_STATUS_CONFLICT"));
    }

    @Test
    void shouldReturn404WhenEmployeeNotFoundOnStatusUpdate() throws Exception {
        when(updateEmployeeStatusUseCase.execute(eq(99L), eq("ACTIVE")))
            .thenThrow(new EmployeeNotFoundException(99L));

        String body = """
            {"status":"ACTIVE"}
            """;

        mockMvc.perform(patch("/api/employees/99/status").contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isNotFound());
    }
}

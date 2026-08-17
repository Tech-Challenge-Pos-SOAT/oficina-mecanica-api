package com.postech.oficinamecanica.interfaces.rest.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.postech.oficinamecanica.application.service.ChangeServiceStatusCommand;
import com.postech.oficinamecanica.application.service.ChangeServiceStatusUseCase;
import com.postech.oficinamecanica.application.service.CreateServiceCommand;
import com.postech.oficinamecanica.application.service.CreateServiceUseCase;
import com.postech.oficinamecanica.application.service.FindServiceByIdUseCase;
import com.postech.oficinamecanica.application.service.ListServicesUseCase;
import com.postech.oficinamecanica.application.service.UpdateServiceCommand;
import com.postech.oficinamecanica.application.service.UpdateServiceUseCase;
import com.postech.oficinamecanica.domain.service.Service;
import com.postech.oficinamecanica.domain.service.ServiceNotFoundException;
import com.postech.oficinamecanica.domain.shared.EntityStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import java.math.BigDecimal;
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

@WebMvcTest(ServiceController.class)
class ServiceControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CreateServiceUseCase createServiceUseCase;

    @MockitoBean
    private UpdateServiceUseCase updateServiceUseCase;

    @MockitoBean
    private FindServiceByIdUseCase findServiceByIdUseCase;

    @MockitoBean
    private ListServicesUseCase listServicesUseCase;

    @MockitoBean
    private ChangeServiceStatusUseCase changeServiceStatusUseCase;

    @MockitoBean
    private ServiceRestMapper mapper;

    private Service aService() {
        return new Service(1L, "Troca de óleo", "desc", new BigDecimal("120.00"),
            EntityStatus.ACTIVE, Instant.now(), Instant.now());
    }

    private ServiceResponse aResponse() {
        return new ServiceResponse(1L, "Troca de óleo", "desc", new BigDecimal("120.00"),
            "ACTIVE", Instant.now(), Instant.now());
    }

    @Test
    void shouldCreateServiceAndReturn201() throws Exception {
        Service service = aService();
        ServiceResponse response = aResponse();
        CreateServiceCommand cmd = new CreateServiceCommand("Troca de óleo", "desc", new BigDecimal("120.00"));

        when(mapper.toCommand(any(CreateServiceRequest.class))).thenReturn(cmd);
        when(createServiceUseCase.execute(cmd)).thenReturn(service);
        when(mapper.toResponse(service)).thenReturn(response);

        String body = objectMapper.writeValueAsString(
            new CreateServiceRequest("Troca de óleo", "desc", new BigDecimal("120.00"))
        );

        mockMvc.perform(post("/api/services")
                .contentType("application/json")
                .content(body))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.name").value("Troca de óleo"))
            .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void shouldRejectCreateWhenPriceIsMissing() throws Exception {
        String body = """
            {"name": "Troca de óleo", "description": "desc"}
            """;

        mockMvc.perform(post("/api/services")
                .contentType("application/json")
                .content(body))
            .andExpect(status().isBadRequest());
    }

    @Test
    void shouldUpdateServiceAndReturn200() throws Exception {
        Service service = aService();
        ServiceResponse response = aResponse();
        UpdateServiceCommand cmd = new UpdateServiceCommand("Troca de óleo", "desc", new BigDecimal("135.00"));

        when(mapper.toCommand(any(UpdateServiceRequest.class))).thenReturn(cmd);
        when(updateServiceUseCase.execute(eq(1L), eq(cmd))).thenReturn(service);
        when(mapper.toResponse(service)).thenReturn(response);

        String body = objectMapper.writeValueAsString(
            new UpdateServiceRequest("Troca de óleo", "desc", new BigDecimal("135.00"))
        );

        mockMvc.perform(put("/api/services/1")
                .contentType("application/json")
                .content(body))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Troca de óleo"));
    }

    @Test
    void shouldReturnServiceById() throws Exception {
        Service service = aService();
        ServiceResponse response = aResponse();

        when(findServiceByIdUseCase.execute(1L)).thenReturn(service);
        when(mapper.toResponse(service)).thenReturn(response);

        mockMvc.perform(get("/api/services/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void shouldReturn404WhenServiceNotFound() throws Exception {
        when(findServiceByIdUseCase.execute(99L)).thenThrow(new ServiceNotFoundException(99L));

        mockMvc.perform(get("/api/services/99"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value("SERVICE_NOT_FOUND"));
    }

    @Test
    void shouldListAllServices() throws Exception {
        Service service = aService();
        ServiceResponse response = aResponse();

        when(listServicesUseCase.execute()).thenReturn(List.of(service));
        when(mapper.toResponse(service)).thenReturn(response);

        mockMvc.perform(get("/api/services"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].name").value("Troca de óleo"));
    }

    @Test
    void shouldChangeServiceStatus() throws Exception {
        Service service = aService();
        ServiceResponse response = new ServiceResponse(1L, "Troca de óleo", "desc",
            new BigDecimal("120.00"), "INACTIVE", Instant.now(), Instant.now());
        ChangeServiceStatusCommand cmd = new ChangeServiceStatusCommand("INACTIVE");

        when(mapper.toCommand(any(ChangeServiceStatusRequest.class))).thenReturn(cmd);
        when(changeServiceStatusUseCase.execute(eq(1L), eq(cmd))).thenReturn(service);
        when(mapper.toResponse(service)).thenReturn(response);

        String body = objectMapper.writeValueAsString(new ChangeServiceStatusRequest("INACTIVE"));

        mockMvc.perform(patch("/api/services/1/status")
                .contentType("application/json")
                .content(body))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("INACTIVE"));
    }
}

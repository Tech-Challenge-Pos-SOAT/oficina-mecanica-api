package com.postech.oficinamecanica.interfaces.rest.serviceorder;

import com.postech.oficinamecanica.application.serviceorder.AddMaterialToServiceOrderUseCase;
import com.postech.oficinamecanica.application.serviceorder.AddServiceToServiceOrderUseCase;
import com.postech.oficinamecanica.application.serviceorder.DeliverServiceOrderUseCase;
import com.postech.oficinamecanica.application.serviceorder.FinishServiceOrderUseCase;
import com.postech.oficinamecanica.application.serviceorder.GetServiceOrderUseCase;
import com.postech.oficinamecanica.application.serviceorder.ListServiceOrdersUseCase;
import com.postech.oficinamecanica.application.serviceorder.OpenServiceOrderCommand;
import com.postech.oficinamecanica.application.serviceorder.OpenServiceOrderUseCase;
import com.postech.oficinamecanica.application.serviceorder.StartServiceOrderDiagnosisUseCase;
import com.postech.oficinamecanica.application.serviceorder.SubmitServiceOrderBudgetUseCase;
import com.postech.oficinamecanica.domain.serviceorder.ServiceOrder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ServiceOrderController.class)
class ServiceOrderControllerTest {
    @Autowired private MockMvc mockMvc;

    @MockitoBean private OpenServiceOrderUseCase openServiceOrderUseCase;
    @MockitoBean private StartServiceOrderDiagnosisUseCase startDiagnosisUseCase;
    @MockitoBean private AddServiceToServiceOrderUseCase addServiceUseCase;
    @MockitoBean private AddMaterialToServiceOrderUseCase addMaterialUseCase;
    @MockitoBean private SubmitServiceOrderBudgetUseCase submitBudgetUseCase;
    @MockitoBean private FinishServiceOrderUseCase finishServiceOrderUseCase;
    @MockitoBean private DeliverServiceOrderUseCase deliverServiceOrderUseCase;
    @MockitoBean private GetServiceOrderUseCase getServiceOrderUseCase;
    @MockitoBean private ListServiceOrdersUseCase listServiceOrdersUseCase;
    @MockitoBean private ServiceOrderRestMapper mapper;

    private ServiceOrder anOrder() {
        return ServiceOrder.open(7L, 3L, 1L);
    }

    private ServiceOrderResponse aResponse(String status) {
        return new ServiceOrderResponse(1L, 7L, 3L, status, new BigDecimal("210.00"),
            List.of(), List.of(), List.of(), Instant.parse("2026-08-29T10:00:00Z"), Instant.parse("2026-08-29T10:00:00Z"));
    }

    @Test
    void shouldOpenServiceOrder() throws Exception {
        ServiceOrder order = anOrder();
        when(openServiceOrderUseCase.execute(any(OpenServiceOrderCommand.class))).thenReturn(order);
        when(mapper.toResponse(order)).thenReturn(aResponse("RECEIVED"));

        mockMvc.perform(post("/api/service-orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"customerDocument\":\"529.982.247-25\",\"plate\":\"ABC-1234\",\"employeeId\":1}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.status").value("RECEIVED"));

        verify(openServiceOrderUseCase).execute(new OpenServiceOrderCommand("529.982.247-25", "ABC-1234", 1L));
    }

    @Test
    void shouldRejectOpeningWithoutCustomerDocument() throws Exception {
        mockMvc.perform(post("/api/service-orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"customerDocument\":\"\",\"plate\":\"ABC-1234\",\"employeeId\":1}"))
            .andExpect(status().isBadRequest());

        verifyNoInteractions(openServiceOrderUseCase);
    }

    @Test
    void shouldAddMaterialToOrder() throws Exception {
        ServiceOrder order = anOrder();
        when(addMaterialUseCase.execute(1L, 9L, 2, 1L)).thenReturn(order);
        when(mapper.toResponse(order)).thenReturn(aResponse("IN_DIAGNOSIS"));

        mockMvc.perform(post("/api/service-orders/1/materials")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"materialId\":9,\"quantity\":2,\"employeeId\":1}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.price").value(210.00));

        verify(addMaterialUseCase).execute(1L, 9L, 2, 1L);
    }

    @Test
    void shouldRejectMaterialWithNonPositiveQuantity() throws Exception {
        mockMvc.perform(post("/api/service-orders/1/materials")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"materialId\":9,\"quantity\":0,\"employeeId\":1}"))
            .andExpect(status().isBadRequest());

        verifyNoInteractions(addMaterialUseCase);
    }

    @Test
    void shouldSubmitBudgetToCustomer() throws Exception {
        ServiceOrder order = anOrder();
        when(submitBudgetUseCase.execute(1L, 1L)).thenReturn(order);
        when(mapper.toResponse(order)).thenReturn(aResponse("AWAITING_APPROVAL"));

        mockMvc.perform(post("/api/service-orders/1/budget")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"employeeId\":1}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("AWAITING_APPROVAL"));

        verify(submitBudgetUseCase).execute(1L, 1L);
    }

    @Test
    void shouldListOrdersFilteringByStatus() throws Exception {
        ServiceOrder order = anOrder();
        when(listServiceOrdersUseCase.execute("IN_EXECUTION")).thenReturn(List.of(order));
        when(mapper.toResponse(order)).thenReturn(aResponse("IN_EXECUTION"));

        mockMvc.perform(get("/api/service-orders?status=IN_EXECUTION"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].status").value("IN_EXECUTION"));

        verify(listServiceOrdersUseCase).execute("IN_EXECUTION");
    }
}

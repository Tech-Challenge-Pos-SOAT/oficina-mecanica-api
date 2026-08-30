package com.postech.oficinamecanica.interfaces.rest.serviceorder;

import com.postech.oficinamecanica.application.serviceorder.ApproveServiceOrderBudgetUseCase;
import com.postech.oficinamecanica.application.serviceorder.GetCustomerServiceOrderUseCase;
import com.postech.oficinamecanica.application.serviceorder.RejectServiceOrderBudgetUseCase;
import com.postech.oficinamecanica.application.serviceorder.TrackServiceOrdersByDocumentUseCase;
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

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PublicServiceOrderController.class)
class PublicServiceOrderControllerTest {
    private static final String DOCUMENT = "529.982.247-25";

    @Autowired private MockMvc mockMvc;

    @MockitoBean private TrackServiceOrdersByDocumentUseCase trackServiceOrdersUseCase;
    @MockitoBean private GetCustomerServiceOrderUseCase getCustomerServiceOrderUseCase;
    @MockitoBean private ApproveServiceOrderBudgetUseCase approveBudgetUseCase;
    @MockitoBean private RejectServiceOrderBudgetUseCase rejectBudgetUseCase;
    @MockitoBean private ServiceOrderRestMapper mapper;

    private ServiceOrder anOrder() {
        return ServiceOrder.open(7L, 3L, 1L);
    }

    private ServiceOrderResponse aResponse(String status) {
        return new ServiceOrderResponse(1L, 7L, 3L, status, new BigDecimal("210.00"),
            List.of(), List.of(), List.of(), Instant.parse("2026-08-29T10:00:00Z"), Instant.parse("2026-08-29T10:00:00Z"));
    }

    @Test
    void shouldTrackOrdersByDocumentWithoutToken() throws Exception {
        ServiceOrder order = anOrder();
        when(trackServiceOrdersUseCase.execute(DOCUMENT)).thenReturn(List.of(order));
        when(mapper.toTrackingResponse(order)).thenReturn(new ServiceOrderTrackingResponse(
            1L, "AWAITING_APPROVAL", new BigDecimal("210.00"),
            Instant.parse("2026-08-29T10:00:00Z"), Instant.parse("2026-08-29T10:00:00Z")));

        mockMvc.perform(get("/public/service-orders?document=" + DOCUMENT))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(1))
            .andExpect(jsonPath("$[0].status").value("AWAITING_APPROVAL"));

        verify(trackServiceOrdersUseCase).execute(DOCUMENT);
    }

    @Test
    void shouldApproveBudget() throws Exception {
        ServiceOrder order = anOrder();
        when(approveBudgetUseCase.execute(1L, DOCUMENT)).thenReturn(order);
        when(mapper.toResponse(order)).thenReturn(aResponse("IN_EXECUTION"));

        mockMvc.perform(post("/public/service-orders/1/approval")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"customerDocument\":\"" + DOCUMENT + "\",\"approved\":true}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("IN_EXECUTION"));

        verify(approveBudgetUseCase).execute(1L, DOCUMENT);
        verifyNoInteractions(rejectBudgetUseCase);
    }

    @Test
    void shouldRejectBudgetKeepingTheReason() throws Exception {
        ServiceOrder order = anOrder();
        when(rejectBudgetUseCase.execute(1L, DOCUMENT, "Preco alto")).thenReturn(order);
        when(mapper.toResponse(order)).thenReturn(aResponse("FINISHED"));

        mockMvc.perform(post("/public/service-orders/1/approval")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"customerDocument\":\"" + DOCUMENT + "\",\"approved\":false,\"reason\":\"Preco alto\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("FINISHED"));

        verify(rejectBudgetUseCase).execute(1L, DOCUMENT, "Preco alto");
        verifyNoInteractions(approveBudgetUseCase);
    }

    @Test
    void shouldRejectDecisionWithoutDocument() throws Exception {
        mockMvc.perform(post("/public/service-orders/1/approval")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"customerDocument\":\"\",\"approved\":true}"))
            .andExpect(status().isBadRequest());

        verifyNoInteractions(approveBudgetUseCase);
    }
}

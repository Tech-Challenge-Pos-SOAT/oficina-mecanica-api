package com.postech.oficinamecanica.interfaces.rest.materialtransaction;

import com.postech.oficinamecanica.application.materialtransaction.ListMaterialTransactionsByMaterialUseCase;
import com.postech.oficinamecanica.domain.materialtransaction.MaterialTransaction;
import com.postech.oficinamecanica.domain.materialtransaction.TransactionType;
import com.postech.oficinamecanica.domain.shared.exceptions.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MaterialTransactionsByMaterialController.class)
class MaterialTransactionsByMaterialControllerTest {
    @Autowired private MockMvc mockMvc;

    @MockitoBean private ListMaterialTransactionsByMaterialUseCase listByMaterialUseCase;
    @MockitoBean private MaterialTransactionRestMapper mapper;

    @Test
    void shouldReturnTransactionsOfMaterial() throws Exception {
        MaterialTransaction tx = new MaterialTransaction(
            1L, 10L, 5L, 100, TransactionType.IN, Instant.parse("2026-08-19T10:30:00Z")
        );
        MaterialTransactionResponse response = new MaterialTransactionResponse(
            1L, 10L, 5L, 100, "IN", Instant.parse("2026-08-19T10:30:00Z")
        );

        when(listByMaterialUseCase.execute(10L, null)).thenReturn(List.of(tx));
        when(mapper.toResponse(tx)).thenReturn(response);

        mockMvc.perform(get("/api/materials/10/transactions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].materialId").value(10))
                .andExpect(jsonPath("$[0].serviceOrderId").value(5))
                .andExpect(jsonPath("$[0].quantity").value(100))
                .andExpect(jsonPath("$[0].type").value("IN"))
                .andExpect(jsonPath("$[0].createdAt").exists());
    }

    @Test
    void shouldPassTypeFilterToUseCase() throws Exception {
        MaterialTransaction tx = new MaterialTransaction(
            2L, 10L, null, 50, TransactionType.OUT, Instant.parse("2026-08-19T11:00:00Z")
        );
        MaterialTransactionResponse response = new MaterialTransactionResponse(
            2L, 10L, null, 50, "OUT", Instant.parse("2026-08-19T11:00:00Z")
        );

        when(listByMaterialUseCase.execute(10L, TransactionType.OUT)).thenReturn(List.of(tx));
        when(mapper.toResponse(tx)).thenReturn(response);

        mockMvc.perform(get("/api/materials/10/transactions").param("type", "OUT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].type").value("OUT"));
    }

    @Test
    void shouldReturnEmptyListWhenMaterialHasNoTransactions() throws Exception {
        when(listByMaterialUseCase.execute(10L, null)).thenReturn(List.of());

        mockMvc.perform(get("/api/materials/10/transactions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void shouldReturn404WhenMaterialDoesNotExist() throws Exception {
        when(listByMaterialUseCase.execute(999L, null))
                .thenThrow(new ResourceNotFoundException("Material", 999L));

        mockMvc.perform(get("/api/materials/999/transactions"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"));
    }

    @Test
    void shouldReturn400WhenTypeIsInvalid() throws Exception {
        mockMvc.perform(get("/api/materials/10/transactions").param("type", "INVALID"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_PARAMETER_TYPE"));

        verifyNoInteractions(listByMaterialUseCase);
    }
}

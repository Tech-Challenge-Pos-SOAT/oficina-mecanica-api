package com.postech.oficinamecanica.interfaces.rest.materialtransaction;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.postech.oficinamecanica.application.materialtransaction.GetMaterialTransactionByIdUseCase;
import com.postech.oficinamecanica.application.materialtransaction.ListMaterialTransactionsUseCase;
import com.postech.oficinamecanica.domain.materialtransaction.MaterialTransaction;
import com.postech.oficinamecanica.domain.materialtransaction.TransactionType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MaterialTransactionController.class)
class MaterialTransactionControllerGetByIdTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ListMaterialTransactionsUseCase listMaterialTransactionsUseCase;

    @MockitoBean
    private MaterialTransactionRestMapper transactionRestMapper;

    @MockitoBean
    private GetMaterialTransactionByIdUseCase getByIdUseCase;

    @Test
    void shouldReturnTransactionWhenFound() throws Exception {
        MaterialTransaction transaction = new MaterialTransaction(
                1L, 2L, null, 10, TransactionType.IN, Instant.parse("2026-08-19T10:30:00Z")
        );
        MaterialTransactionResponse response = new MaterialTransactionResponse(
                1L, 2L, null, 10, "IN", Instant.parse("2026-08-19T10:30:00Z")
        );
        when(getByIdUseCase.execute(1L)).thenReturn(transaction);
        when(transactionRestMapper.toResponse(transaction)).thenReturn(response);

        mockMvc.perform(get("/api/material-transactions/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.materialId").value(2))
                .andExpect(jsonPath("$.quantity").value(10))
                .andExpect(jsonPath("$.type").value("IN"))
                .andExpect(jsonPath("$.serviceOrderId").doesNotExist());
    }

    @Test
    void shouldReturn404WhenTransactionNotFound() throws Exception {
        when(getByIdUseCase.execute(999L))
                .thenThrow(new com.postech.oficinamecanica.domain.shared.exceptions.ResourceNotFoundException("MaterialTransaction", 999L));

        mockMvc.perform(get("/api/material-transactions/999"))
                .andExpect(status().isNotFound());
    }
}

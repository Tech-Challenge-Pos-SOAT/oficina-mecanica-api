package com.postech.oficinamecanica.interfaces.rest.materialtransaction;

import com.postech.oficinamecanica.application.materialtransaction.GetMaterialTransactionByIdUseCase;
import com.postech.oficinamecanica.application.materialtransaction.ListMaterialTransactionsUseCase;
import com.postech.oficinamecanica.domain.materialtransaction.MaterialTransaction;
import com.postech.oficinamecanica.domain.materialtransaction.TransactionType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import java.time.Instant;
import java.util.List;

@WebMvcTest(MaterialTransactionController.class)
class MaterialTransactionControllerTest {
    @Autowired private MockMvc mockMvc;
    @MockitoBean private ListMaterialTransactionsUseCase useCase;
    @MockitoBean private GetMaterialTransactionByIdUseCase getByIdUseCase;
    @MockitoBean private MaterialTransactionRestMapper mapper;

    @Test
    void shouldListAllTransactions() throws Exception {
        MaterialTransaction tx = new MaterialTransaction(
            1L, 10L, 5L, 100, TransactionType.OUT, Instant.parse("2026-08-19T10:30:00Z")
        );
        MaterialTransactionResponse response = new MaterialTransactionResponse(
            1L, 10L, 5L, 100, "OUT", Instant.parse("2026-08-19T10:30:00Z")
        );

        when(useCase.execute(null)).thenReturn(List.of(tx));
        when(mapper.toResponse(tx)).thenReturn(response);

        mockMvc.perform(get("/api/material-transactions"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(1))
            .andExpect(jsonPath("$[0].materialId").value(10))
            .andExpect(jsonPath("$[0].type").value("OUT"));

        verify(useCase).execute(null);
    }

    @Test
    void shouldFilterByType() throws Exception {
        MaterialTransaction tx = new MaterialTransaction(
                2L, 20L, null, 50, TransactionType.IN, Instant.parse("2026-08-19T11:00:00Z")
        );
        MaterialTransactionResponse response = new MaterialTransactionResponse(
                2L, 20L, null, 50, "IN", Instant.parse("2026-08-19T11:00:00Z")
        );

        when(useCase.execute(TransactionType.IN)).thenReturn(List.of(tx));
        when(mapper.toResponse(tx)).thenReturn(response);

        mockMvc.perform(get("/api/material-transactions?type=IN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].type").value("IN"));

        verify(useCase).execute(TransactionType.IN);
    }

    @Test
    void shouldReturn400OnInvalidType() throws Exception {
        mockMvc.perform(get("/api/material-transactions?type=INVALID"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(useCase);
    }
}

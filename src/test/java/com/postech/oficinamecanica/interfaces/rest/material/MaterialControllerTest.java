package com.postech.oficinamecanica.interfaces.rest.material;

import com.postech.oficinamecanica.application.material.ListMaterialsUseCase;
import com.postech.oficinamecanica.domain.material.Material;
import com.postech.oficinamecanica.domain.shared.EntityStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MaterialController.class)
class MaterialControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ListMaterialsUseCase listMaterialsUseCase;

    @MockitoBean
    private MaterialRestMapper mapper;

    @Test
    void shouldReturnMaterialsWhenNoStatusFilterProvided() throws Exception {
        Material material = aMaterial(1L, "Filtro de Oleo", EntityStatus.ACTIVE);
        when(listMaterialsUseCase.execute(null)).thenReturn(List.of(material));
        when(mapper.toResponse(material)).thenReturn(aResponse(1L, "Filtro de Oleo", "ACTIVE"));

        mockMvc.perform(get("/api/materials"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(1))
            .andExpect(jsonPath("$[0].name").value("Filtro de Oleo"))
            .andExpect(jsonPath("$[0].price").value(32.50))
            .andExpect(jsonPath("$[0].stockQuantity").value(25))
            .andExpect(jsonPath("$[0].stockMinimum").value(5))
            .andExpect(jsonPath("$[0].status").value("ACTIVE"));
    }

    @Test
    void shouldForwardStatusFilterToUseCase() throws Exception {
        Material material = aMaterial(5L, "Bateria 60Ah", EntityStatus.INACTIVE);
        when(listMaterialsUseCase.execute("INACTIVE")).thenReturn(List.of(material));
        when(mapper.toResponse(material)).thenReturn(aResponse(5L, "Bateria 60Ah", "INACTIVE"));

        mockMvc.perform(get("/api/materials?status=INACTIVE"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].status").value("INACTIVE"));
    }

    @Test
    void shouldReturnEmptyListWhenNoMaterialMatchesFilter() throws Exception {
        when(listMaterialsUseCase.execute(null)).thenReturn(List.of());

        mockMvc.perform(get("/api/materials"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void shouldReturnBadRequestWhenStatusIsUnknown() throws Exception {
        when(listMaterialsUseCase.execute("ARCHIVED"))
            .thenThrow(new IllegalArgumentException("No enum constant ARCHIVED"));

        mockMvc.perform(get("/api/materials?status=ARCHIVED"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("INVALID_STATUS"));
    }

    private static Material aMaterial(Long id, String name, EntityStatus status) {
        return new Material(id, name, "Descricao de catalogo", new BigDecimal("32.50"),
            25, 5, status, Instant.now(), Instant.now());
    }

    private static MaterialResponse aResponse(Long id, String name, String status) {
        return new MaterialResponse(id, name, "Descricao de catalogo", new BigDecimal("32.50"),
            25, 5, status, Instant.now(), Instant.now());
    }
}

package com.postech.oficinamecanica.interfaces.rest.material;

import com.postech.oficinamecanica.application.material.ChangeMaterialStatusUseCase;
import com.postech.oficinamecanica.application.material.CreateMaterialUseCase;
import com.postech.oficinamecanica.application.material.GetMaterialUseCase;
import com.postech.oficinamecanica.application.material.ListLowStockMaterialsUseCase;
import com.postech.oficinamecanica.application.material.ListMaterialsUseCase;
import com.postech.oficinamecanica.application.material.StockEntryUseCase;
import com.postech.oficinamecanica.application.material.UpdateMaterialUseCase;
import com.postech.oficinamecanica.domain.material.Material;
import com.postech.oficinamecanica.domain.shared.EntityStatus;
import com.postech.oficinamecanica.domain.shared.exceptions.ResourceNotFoundException;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MaterialController.class)
class MaterialControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ListMaterialsUseCase listMaterialsUseCase;

    @MockitoBean
    private ListLowStockMaterialsUseCase listLowStockMaterialsUseCase;

    @MockitoBean
    private GetMaterialUseCase getMaterialUseCase;

    @MockitoBean
    private ChangeMaterialStatusUseCase changeMaterialStatusUseCase;

    @MockitoBean
    private CreateMaterialUseCase createMaterialUseCase;

    @MockitoBean
    private UpdateMaterialUseCase updateMaterialUseCase;

    @MockitoBean
    private StockEntryUseCase stockEntryUseCase;

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

    @Test
    void shouldReturnMaterialWhenFoundById() throws Exception {
        Long id = 1L;
        Material material = aMaterial(id, "Filtro de Oleo", EntityStatus.ACTIVE);
        when(getMaterialUseCase.execute(id)).thenReturn(material);
        when(mapper.toResponse(material)).thenReturn(aResponse(id, "Filtro de Oleo", "ACTIVE"));

        mockMvc.perform(get("/api/materials/{id}", id))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(id))
            .andExpect(jsonPath("$.name").value("Filtro de Oleo"))
            .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void shouldReturnNotFoundWhenMaterialDoesNotExist() throws Exception {
        Long id = 999L;
        when(getMaterialUseCase.execute(id)).thenThrow(new ResourceNotFoundException("Material", id));

        mockMvc.perform(get("/api/materials/{id}", id))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"))
            .andExpect(jsonPath("$.message").value("Material com identificador '999' não foi encontrado."));
    }

    @Test
    void shouldUpdateMaterialStatus() throws Exception {
        Material material = aMaterial(1L, "Filtro de Oleo", EntityStatus.INACTIVE);
        when(changeMaterialStatusUseCase.execute(1L, "INACTIVE")).thenReturn(material);
        when(mapper.toResponse(material)).thenReturn(aResponse(1L, "Filtro de Oleo", "INACTIVE"));

        mockMvc.perform(patch("/api/materials/1/status")
                .contentType("application/json")
                .content("{\"status\": \"INACTIVE\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.status").value("INACTIVE"));
    }

    @Test
    void shouldCreateMaterial() throws Exception {
        Material material = aMaterial(10L, "Amortecedor", EntityStatus.ACTIVE);
        when(createMaterialUseCase.execute("Amortecedor", "Desc", new BigDecimal("450.00"), 10, 2)).thenReturn(material);
        when(mapper.toResponse(material)).thenReturn(aResponse(10L, "Amortecedor", "ACTIVE"));

        String payload = """
            {
                "name": "Amortecedor",
                "description": "Desc",
                "price": 450.00,
                "stockQuantity": 10,
                "stockMinimum": 2
            }
            """;

        mockMvc.perform(post("/api/materials")
                .contentType("application/json")
                .content(payload))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(10))
            .andExpect(jsonPath("$.name").value("Amortecedor"));
    }

    @Test
    void shouldUpdateMaterial() throws Exception {
        Material material = aMaterial(1L, "Amortecedor", EntityStatus.ACTIVE);
        when(updateMaterialUseCase.execute(1L, "Amortecedor", "Desc", new BigDecimal("520.00"), 15, 3)).thenReturn(material);
        when(mapper.toResponse(material)).thenReturn(aResponse(1L, "Amortecedor", "ACTIVE"));

        String payload = """
            {
                "name": "Amortecedor",
                "description": "Desc",
                "price": 520.00,
                "stockQuantity": 15,
                "stockMinimum": 3
            }
            """;

        mockMvc.perform(put("/api/materials/1")
                .contentType("application/json")
                .content(payload))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.name").value("Amortecedor"));
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

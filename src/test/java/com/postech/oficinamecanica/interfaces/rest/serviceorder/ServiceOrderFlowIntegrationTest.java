package com.postech.oficinamecanica.interfaces.rest.serviceorder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.postech.oficinamecanica.application.auth.TokenProvider;
import com.postech.oficinamecanica.domain.employee.Employee;
import com.postech.oficinamecanica.domain.employee.EmployeeRole;
import com.postech.oficinamecanica.domain.shared.EntityStatus;
import com.postech.oficinamecanica.infrastructure.persistence.customer.CustomerJpaEntity;
import com.postech.oficinamecanica.infrastructure.persistence.customer.CustomerJpaRepository;
import com.postech.oficinamecanica.infrastructure.persistence.employee.EmployeeJpaEntity;
import com.postech.oficinamecanica.infrastructure.persistence.employee.EmployeeJpaRepository;
import com.postech.oficinamecanica.infrastructure.persistence.material.MaterialJpaEntity;
import com.postech.oficinamecanica.infrastructure.persistence.material.MaterialJpaRepository;
import com.postech.oficinamecanica.infrastructure.persistence.materialtransaction.MaterialTransactionJpaRepository;
import com.postech.oficinamecanica.infrastructure.persistence.service.ServiceJpaEntity;
import com.postech.oficinamecanica.infrastructure.persistence.service.ServiceJpaRepository;
import com.postech.oficinamecanica.infrastructure.persistence.serviceorder.ServiceOrderJpaRepository;
import com.postech.oficinamecanica.infrastructure.persistence.vehicle.VehicleJpaEntity;
import com.postech.oficinamecanica.infrastructure.persistence.vehicle.VehicleJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Fluxo completo da OS contra Postgres real: abertura pelo CPF/CNPJ + placa,
 * diagnostico, itens, orcamento automatico, envio, acompanhamento e aprovacao
 * pelo cliente (sem token), baixa de estoque, finalizacao e entrega.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=validate",
        "spring.docker.compose.enabled=false"
})
class ServiceOrderFlowIntegrationTest {

    private static final String DOCUMENT = "529.982.247-25";
    private static final String PLATE = "XYZ-9876";

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private TokenProvider tokenProvider;
    @Autowired private CustomerJpaRepository customerRepository;
    @Autowired private VehicleJpaRepository vehicleRepository;
    @Autowired private ServiceJpaRepository serviceRepository;
    @Autowired private MaterialJpaRepository materialRepository;
    @Autowired private ServiceOrderJpaRepository serviceOrderRepository;
    @Autowired private MaterialTransactionJpaRepository materialTransactionRepository;
    @Autowired private EmployeeJpaRepository employeeRepository;

    private String authHeader;
    private Long employeeId;
    private Long serviceId;
    private Long materialId;

    @BeforeEach
    void setUp() {
        materialTransactionRepository.deleteAll();
        serviceOrderRepository.deleteAll();
        vehicleRepository.deleteAll();
        materialRepository.deleteAll();
        serviceRepository.deleteAll();

        CustomerJpaEntity customer = customerRepository.findByDocument(DOCUMENT).orElseThrow();
        EmployeeJpaEntity employee = employeeRepository.findByEmail("carlos.souza@oficina.com").orElseThrow();
        employeeId = employee.getId();

        Instant now = Instant.now();
        vehicleRepository.save(new VehicleJpaEntity(null, customer.getId(), "Fiat", "Uno", PLATE,
                2019, EntityStatus.ACTIVE, now, now));
        serviceId = serviceRepository.save(new ServiceJpaEntity(null, "Troca de oleo", "Motor flex",
                new BigDecimal("150.00"), EntityStatus.ACTIVE, now, now)).getId();
        materialId = materialRepository.save(new MaterialJpaEntity(null, "Oleo 5W30", "Galao 4 litros",
                new BigDecimal("30.00"), 10, 2, EntityStatus.ACTIVE, now, now)).getId();

        authHeader = "Bearer " + tokenProvider.generateToken(new Employee(
                employeeId, "Carlos Souza", "carlos.souza@oficina.com", "hash",
                EmployeeRole.ATTENDANT, EntityStatus.ACTIVE, now, now));
    }

    private Long openOrder() throws Exception {
        String body = mockMvc.perform(post("/api/service-orders")
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"customerDocument\":\"" + DOCUMENT + "\",\"plate\":\"" + PLATE
                                + "\",\"employeeId\":" + employeeId + "}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("RECEIVED"))
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readTree(body).get("id").asLong();
    }

    private void employeeAction(String path, Long orderId, String expectedStatus) throws Exception {
        mockMvc.perform(post("/api/service-orders/" + orderId + path)
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"employeeId\":" + employeeId + "}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(expectedStatus));
    }

    @Test
    void shouldRunTheWholeServiceOrderFlowFromOpeningToDelivery() throws Exception {
        Long orderId = openOrder();

        employeeAction("/diagnosis", orderId, "IN_DIAGNOSIS");

        mockMvc.perform(post("/api/service-orders/" + orderId + "/services")
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"serviceId\":" + serviceId + ",\"employeeId\":" + employeeId + "}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.price").value(150.00));

        mockMvc.perform(post("/api/service-orders/" + orderId + "/materials")
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"materialId\":" + materialId + ",\"quantity\":2,\"employeeId\":" + employeeId + "}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.price").value(210.00));

        employeeAction("/budget", orderId, "AWAITING_APPROVAL");

        // Cliente acompanha sem token, so com o CPF/CNPJ.
        mockMvc.perform(get("/public/service-orders?document=" + DOCUMENT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].status").value("AWAITING_APPROVAL"))
                .andExpect(jsonPath("$[0].price").value(210.00));

        mockMvc.perform(post("/public/service-orders/" + orderId + "/approval")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"customerDocument\":\"" + DOCUMENT + "\",\"approved\":true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_EXECUTION"));

        assertThat(materialRepository.findById(materialId).orElseThrow().getStockQuantity()).isEqualTo(8);
        assertThat(materialTransactionRepository.findByMaterialIdOrderByIdAsc(materialId)).hasSize(1);

        mockMvc.perform(post("/api/service-orders/" + orderId + "/completion")
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"employeeId\":" + employeeId + ",\"observation\":\"Servicos concluidos\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("FINISHED"));

        employeeAction("/delivery", orderId, "DELIVERED");

        mockMvc.perform(get("/api/service-orders/" + orderId).header("Authorization", authHeader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.history", hasSize(6)))
                .andExpect(jsonPath("$.history[0].status").value("RECEIVED"))
                .andExpect(jsonPath("$.history[5].status").value("DELIVERED"))
                .andExpect(jsonPath("$.materials[0].total").value(60.00));
    }

    @Test
    void shouldCancelOrderWhenStockIsInsufficient() throws Exception {
        Long orderId = openOrder();
        employeeAction("/diagnosis", orderId, "IN_DIAGNOSIS");

        mockMvc.perform(post("/api/service-orders/" + orderId + "/materials")
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"materialId\":" + materialId + ",\"quantity\":50,\"employeeId\":" + employeeId + "}"))
                .andExpect(status().isOk());

        employeeAction("/budget", orderId, "AWAITING_APPROVAL");

        mockMvc.perform(post("/public/service-orders/" + orderId + "/approval")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"customerDocument\":\"" + DOCUMENT + "\",\"approved\":true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));

        assertThat(materialRepository.findById(materialId).orElseThrow().getStockQuantity()).isEqualTo(10);
        mockMvc.perform(get("/public/service-orders/" + orderId + "?document=" + DOCUMENT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    void shouldCancelOrderWhenTheServiceLeftTheCatalog() throws Exception {
        Long orderId = openOrder();
        employeeAction("/diagnosis", orderId, "IN_DIAGNOSIS");

        mockMvc.perform(post("/api/service-orders/" + orderId + "/services")
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"serviceId\":" + serviceId + ",\"employeeId\":" + employeeId + "}"))
                .andExpect(status().isOk());

        employeeAction("/budget", orderId, "AWAITING_APPROVAL");

        mockMvc.perform(patch("/api/services/" + serviceId + "/status")
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"INACTIVE\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/public/service-orders/" + orderId + "/approval")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"customerDocument\":\"" + DOCUMENT + "\",\"approved\":true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    void shouldKeepTheApprovedWorkWhenTheCustomerRejectsAnAdditionalRepair() throws Exception {
        Long orderId = openOrder();
        employeeAction("/diagnosis", orderId, "IN_DIAGNOSIS");

        mockMvc.perform(post("/api/service-orders/" + orderId + "/materials")
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"materialId\":" + materialId + ",\"quantity\":2,\"employeeId\":" + employeeId + "}"))
                .andExpect(status().isOk());
        employeeAction("/budget", orderId, "AWAITING_APPROVAL");
        mockMvc.perform(post("/public/service-orders/" + orderId + "/approval")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"customerDocument\":\"" + DOCUMENT + "\",\"approved\":true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_EXECUTION"));

        // reparo adicional
        mockMvc.perform(post("/api/service-orders/" + orderId + "/materials")
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"materialId\":" + materialId + ",\"quantity\":3,\"employeeId\":" + employeeId + "}"))
                .andExpect(status().isOk());
        employeeAction("/budget", orderId, "AWAITING_APPROVAL");

        mockMvc.perform(post("/public/service-orders/" + orderId + "/approval")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"customerDocument\":\"" + DOCUMENT + "\",\"approved\":false,"
                                + "\"reason\":\"Nao quero o servico extra\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_EXECUTION"))
                .andExpect(jsonPath("$.materials", hasSize(1)));

        // so a primeira baixa aconteceu: 10 - 2
        assertThat(materialRepository.findById(materialId).orElseThrow().getStockQuantity()).isEqualTo(8);
    }

    @Test
    void shouldReturnMaterialsToStockWhenAnExecutingOrderIsCancelled() throws Exception {
        Long orderId = openOrder();
        employeeAction("/diagnosis", orderId, "IN_DIAGNOSIS");
        mockMvc.perform(post("/api/service-orders/" + orderId + "/materials")
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"materialId\":" + materialId + ",\"quantity\":2,\"employeeId\":" + employeeId + "}"))
                .andExpect(status().isOk());
        employeeAction("/budget", orderId, "AWAITING_APPROVAL");
        mockMvc.perform(post("/public/service-orders/" + orderId + "/approval")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"customerDocument\":\"" + DOCUMENT + "\",\"approved\":true}"))
                .andExpect(status().isOk());
        assertThat(materialRepository.findById(materialId).orElseThrow().getStockQuantity()).isEqualTo(8);

        mockMvc.perform(post("/api/service-orders/" + orderId + "/cancellation")
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"employeeId\":" + employeeId + ",\"reason\":\"Cliente desistiu\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));

        assertThat(materialRepository.findById(materialId).orElseThrow().getStockQuantity()).isEqualTo(10);
        assertThat(materialTransactionRepository.findByMaterialIdOrderByIdAsc(materialId)).hasSize(2);
    }

    @Test
    void shouldRejectTrackingWithAnotherCustomerDocument() throws Exception {
        Long orderId = openOrder();

        mockMvc.perform(get("/public/service-orders/" + orderId + "?document=111.444.777-35"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("SERVICE_ORDER_ACCESS_DENIED"));
    }

    @Test
    void shouldRequireTokenOnInternalEndpoints() throws Exception {
        mockMvc.perform(get("/api/service-orders"))
                .andExpect(status().isUnauthorized());
    }
}

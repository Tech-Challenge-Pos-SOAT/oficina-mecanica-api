package com.postech.oficinamecanica.interfaces.rest.customer;

import com.postech.oficinamecanica.application.customer.ListCustomersUseCase;
import com.postech.oficinamecanica.domain.customer.Customer;
import com.postech.oficinamecanica.domain.customer.Document;
import com.postech.oficinamecanica.domain.shared.EntityStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import java.time.Instant;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CustomerController.class)
class CustomerControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ListCustomersUseCase listCustomersUseCase;

    @MockitoBean
    private CustomerRestMapper mapper;

    @Test
    void shouldReturnActiveCustomersWhenNoStatusFilterProvided() throws Exception {
        Customer customer = new Customer(
            1L,
            new Document("52998224725"),
            "Maria Souza",
            "11987654321",
            "maria@email.com",
            EntityStatus.ACTIVE,
            Instant.now(),
            Instant.now()
        );

        CustomerResponse response = new CustomerResponse(
            1L,
            "Maria Souza",
            "52998224725",
            "11987654321",
            "maria@email.com",
            "ACTIVE",
            Instant.now(),
            Instant.now()
        );

        when(listCustomersUseCase.execute(null))
            .thenReturn(List.of(customer));
        when(mapper.toResponse(customer))
            .thenReturn(response);

        mockMvc.perform(get("/api/customers"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(1))
            .andExpect(jsonPath("$[0].name").value("Maria Souza"))
            .andExpect(jsonPath("$[0].status").value("ACTIVE"));
    }

    @Test
    void shouldReturnActiveCustomersWhenStatusFilterIsActive() throws Exception {
        Customer customer = new Customer(
            1L,
            new Document("52998224725"),
            "Maria Souza",
            "11987654321",
            "maria@email.com",
            EntityStatus.ACTIVE,
            Instant.now(),
            Instant.now()
        );

        CustomerResponse response = new CustomerResponse(
            1L,
            "Maria Souza",
            "52998224725",
            "11987654321",
            "maria@email.com",
            "ACTIVE",
            Instant.now(),
            Instant.now()
        );

        when(listCustomersUseCase.execute("active"))
            .thenReturn(List.of(customer));
        when(mapper.toResponse(customer))
            .thenReturn(response);

        mockMvc.perform(get("/api/customers?status=active"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].name").value("Maria Souza"));
    }

    @Test
    void shouldReturnInactiveCustomersWhenStatusFilterIsInactive() throws Exception {
        Customer customer = new Customer(
            2L,
            new Document("98765432109"),
            "João Silva",
            "11912345678",
            "joao@email.com",
            EntityStatus.INACTIVE,
            Instant.now(),
            Instant.now()
        );

        CustomerResponse response = new CustomerResponse(
            2L,
            "João Silva",
            "98765432109",
            "11912345678",
            "joao@email.com",
            "INACTIVE",
            Instant.now(),
            Instant.now()
        );

        when(listCustomersUseCase.execute("INACTIVE"))
            .thenReturn(List.of(customer));
        when(mapper.toResponse(customer))
            .thenReturn(response);

        mockMvc.perform(get("/api/customers?status=INACTIVE"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].status").value("INACTIVE"));
    }

    @Test
    void shouldReturnEmptyListWhenNoCustomersMatchFilter() throws Exception {
        when(listCustomersUseCase.execute(null))
            .thenReturn(List.of());

        mockMvc.perform(get("/api/customers"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void shouldReturnMultipleCustomersWhenFound() throws Exception {
        Customer customer1 = new Customer(
            1L,
            new Document("52998224725"),
            "Maria Souza",
            "11987654321",
            "maria@email.com",
            EntityStatus.ACTIVE,
            Instant.now(),
            Instant.now()
        );

        Customer customer2 = new Customer(
            2L,
            new Document("12345678901"),
            "Pedro Santos",
            "11987654322",
            "pedro@email.com",
            EntityStatus.ACTIVE,
            Instant.now(),
            Instant.now()
        );

        CustomerResponse response1 = new CustomerResponse(
            1L, "Maria Souza", "52998224725", "11987654321", "maria@email.com", "ACTIVE", Instant.now(), Instant.now()
        );

        CustomerResponse response2 = new CustomerResponse(
            2L, "Pedro Santos", "12345678901", "11987654322", "pedro@email.com", "ACTIVE", Instant.now(), Instant.now()
        );

        when(listCustomersUseCase.execute(null))
            .thenReturn(List.of(customer1, customer2));
        when(mapper.toResponse(customer1))
            .thenReturn(response1);
        when(mapper.toResponse(customer2))
            .thenReturn(response2);

        mockMvc.perform(get("/api/customers"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].name").value("Maria Souza"))
            .andExpect(jsonPath("$[1].name").value("Pedro Santos"));
    }
}

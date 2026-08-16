package com.postech.oficinamecanica.interfaces.rest.customer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.postech.oficinamecanica.application.customer.ChangeCustomerStatusCommand;
import com.postech.oficinamecanica.application.customer.ChangeCustomerStatusUseCase;
import com.postech.oficinamecanica.application.customer.CreateCustomerCommand;
import com.postech.oficinamecanica.application.customer.CreateCustomerUseCase;
import com.postech.oficinamecanica.application.customer.GetCustomerUseCase;
import com.postech.oficinamecanica.application.customer.ListCustomersUseCase;
import com.postech.oficinamecanica.application.customer.UpdateCustomerCommand;
import com.postech.oficinamecanica.application.customer.UpdateCustomerUseCase;
import com.postech.oficinamecanica.domain.customer.Customer;
import com.postech.oficinamecanica.domain.customer.CustomerNotFoundException;
import com.postech.oficinamecanica.domain.customer.Document;
import com.postech.oficinamecanica.domain.customer.DuplicateDocumentException;
import com.postech.oficinamecanica.domain.shared.EntityStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CustomerController.class)
class CustomerControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CreateCustomerUseCase createCustomerUseCase;

    @MockitoBean
    private UpdateCustomerUseCase updateCustomerUseCase;

    @MockitoBean
    private GetCustomerUseCase getCustomerUseCase;

    @MockitoBean
    private ListCustomersUseCase listCustomersUseCase;

    @MockitoBean
    private ChangeCustomerStatusUseCase changeCustomerStatusUseCase;

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
            new Document("11144477735"),
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
            "11144477735",
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
            new Document("12345678909"),
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
            2L, "Pedro Santos", "12345678909", "11987654322", "pedro@email.com", "ACTIVE", Instant.now(), Instant.now()
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

    @Test
    void shouldCreateCustomerAndReturn201() throws Exception {
        CreateCustomerRequest request = new CreateCustomerRequest(
            "Maria Souza", "529.982.247-25", "11987654321", "maria@email.com"
        );
        Customer customer = new Customer(
            1L, new Document("52998224725"), "Maria Souza", "11987654321", "maria@email.com",
            EntityStatus.ACTIVE, Instant.now(), Instant.now()
        );
        CustomerResponse response = new CustomerResponse(
            1L, "Maria Souza", "529.982.247-25", "11987654321", "maria@email.com", "ACTIVE", Instant.now(), Instant.now()
        );

        when(mapper.toCommand(request)).thenReturn(new CreateCustomerCommand(
            "529.982.247-25", "Maria Souza", "11987654321", "maria@email.com"
        ));
        when(createCustomerUseCase.execute(any())).thenReturn(customer);
        when(mapper.toResponse(customer)).thenReturn(response);

        mockMvc.perform(post("/api/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void shouldReturn400WhenCreateRequestHasBlankName() throws Exception {
        CreateCustomerRequest request = new CreateCustomerRequest(
            "", "529.982.247-25", "11987654321", "maria@email.com"
        );

        mockMvc.perform(post("/api/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn409WhenDocumentAlreadyExists() throws Exception {
        CreateCustomerRequest request = new CreateCustomerRequest(
            "Maria Souza", "529.982.247-25", "11987654321", "maria@email.com"
        );

        when(mapper.toCommand(request)).thenReturn(new CreateCustomerCommand(
            "529.982.247-25", "Maria Souza", "11987654321", "maria@email.com"
        ));
        when(createCustomerUseCase.execute(any()))
            .thenThrow(new DuplicateDocumentException(new Document("52998224725")));

        mockMvc.perform(post("/api/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.code").value("DUPLICATE_DOCUMENT"));
    }

    @Test
    void shouldUpdateCustomerAndReturn200() throws Exception {
        UpdateCustomerRequest request = new UpdateCustomerRequest("Maria Lima", "11999998888", "maria.lima@email.com");
        Customer customer = new Customer(
            1L, new Document("52998224725"), "Maria Lima", "11999998888", "maria.lima@email.com",
            EntityStatus.ACTIVE, Instant.now(), Instant.now()
        );
        CustomerResponse response = new CustomerResponse(
            1L, "Maria Lima", "529.982.247-25", "11999998888", "maria.lima@email.com", "ACTIVE", Instant.now(), Instant.now()
        );

        when(mapper.toCommand(1L, request)).thenReturn(new UpdateCustomerCommand(
            1L, "Maria Lima", "11999998888", "maria.lima@email.com"
        ));
        when(updateCustomerUseCase.execute(any())).thenReturn(customer);
        when(mapper.toResponse(customer)).thenReturn(response);

        mockMvc.perform(put("/api/customers/{id}", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Maria Lima"));
    }

    @Test
    void shouldReturn404WhenUpdatingCustomerThatDoesNotExist() throws Exception {
        UpdateCustomerRequest request = new UpdateCustomerRequest("Maria Lima", "11999998888", "maria.lima@email.com");

        when(mapper.toCommand(99L, request)).thenReturn(new UpdateCustomerCommand(
            99L, "Maria Lima", "11999998888", "maria.lima@email.com"
        ));
        when(updateCustomerUseCase.execute(any())).thenThrow(new CustomerNotFoundException(99L));

        mockMvc.perform(put("/api/customers/{id}", 99L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value("CUSTOMER_NOT_FOUND"));
    }

    @Test
    void shouldReturnCustomerByIdWhenFound() throws Exception {
        Customer customer = new Customer(
            1L, new Document("52998224725"), "Maria Souza", "11987654321", "maria@email.com",
            EntityStatus.ACTIVE, Instant.now(), Instant.now()
        );
        CustomerResponse response = new CustomerResponse(
            1L, "Maria Souza", "529.982.247-25", "11987654321", "maria@email.com", "ACTIVE", Instant.now(), Instant.now()
        );

        when(getCustomerUseCase.execute(1L)).thenReturn(customer);
        when(mapper.toResponse(customer)).thenReturn(response);

        mockMvc.perform(get("/api/customers/{id}", 1L))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void shouldReturn404WhenCustomerByIdNotFound() throws Exception {
        when(getCustomerUseCase.execute(99L)).thenThrow(new CustomerNotFoundException(99L));

        mockMvc.perform(get("/api/customers/{id}", 99L))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value("CUSTOMER_NOT_FOUND"));
    }

    @Test
    void shouldChangeCustomerStatusAndReturn200() throws Exception {
        ChangeCustomerStatusRequest request = new ChangeCustomerStatusRequest("INACTIVE");
        Customer customer = new Customer(
            1L, new Document("52998224725"), "Maria Souza", "11987654321", "maria@email.com",
            EntityStatus.INACTIVE, Instant.now(), Instant.now()
        );
        CustomerResponse response = new CustomerResponse(
            1L, "Maria Souza", "529.982.247-25", "11987654321", "maria@email.com", "INACTIVE", Instant.now(), Instant.now()
        );

        when(mapper.toCommand(1L, request)).thenReturn(new ChangeCustomerStatusCommand(1L, "INACTIVE"));
        when(changeCustomerStatusUseCase.execute(any())).thenReturn(customer);
        when(mapper.toResponse(customer)).thenReturn(response);

        mockMvc.perform(patch("/api/customers/{id}/status", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("INACTIVE"));
    }

    @Test
    void shouldReturn409WhenReactivatingCustomerThatIsAlreadyActive() throws Exception {
        ChangeCustomerStatusRequest request = new ChangeCustomerStatusRequest("ACTIVE");

        when(mapper.toCommand(1L, request)).thenReturn(new ChangeCustomerStatusCommand(1L, "ACTIVE"));
        when(changeCustomerStatusUseCase.execute(any()))
            .thenThrow(new com.postech.oficinamecanica.domain.customer.CustomerAlreadyActiveException(1L));

        mockMvc.perform(patch("/api/customers/{id}/status", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.code").value("CUSTOMER_ALREADY_ACTIVE"));
    }
}

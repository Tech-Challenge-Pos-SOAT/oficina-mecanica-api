package com.postech.oficinamecanica.interfaces.rest.customer;

import com.postech.oficinamecanica.application.customer.ChangeCustomerStatusCommand;
import com.postech.oficinamecanica.application.customer.CreateCustomerCommand;
import com.postech.oficinamecanica.application.customer.UpdateCustomerCommand;
import com.postech.oficinamecanica.domain.customer.Customer;
import com.postech.oficinamecanica.domain.customer.Document;
import com.postech.oficinamecanica.domain.shared.EntityStatus;
import org.junit.jupiter.api.Test;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class CustomerRestMapperTest {
    private final CustomerRestMapper mapper = new CustomerRestMapperImpl();

    @Test
    void shouldMapCustomerToResponse() {
        Customer customer = new Customer(
            1L, new Document("529.982.247-25"), "Maria Souza", "11987654321", "maria@email.com",
            EntityStatus.ACTIVE, Instant.now(), Instant.now()
        );

        CustomerResponse response = mapper.toResponse(customer);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.document()).isEqualTo("529.982.247-25");
        assertThat(response.status()).isEqualTo("ACTIVE");
    }

    @Test
    void shouldMapCreateRequestToCommand() {
        CreateCustomerRequest request = new CreateCustomerRequest(
            "Maria Souza", "529.982.247-25", "11987654321", "maria@email.com"
        );

        CreateCustomerCommand cmd = mapper.toCommand(request);

        assertThat(cmd.document()).isEqualTo("529.982.247-25");
        assertThat(cmd.name()).isEqualTo("Maria Souza");
    }

    @Test
    void shouldMapUpdateRequestToCommandWithId() {
        UpdateCustomerRequest request = new UpdateCustomerRequest("Maria Lima", "11999998888", "maria.lima@email.com");

        UpdateCustomerCommand cmd = mapper.toCommand(1L, request);

        assertThat(cmd.id()).isEqualTo(1L);
        assertThat(cmd.name()).isEqualTo("Maria Lima");
    }

    @Test
    void shouldMapChangeStatusRequestToCommandWithId() {
        ChangeCustomerStatusRequest request = new ChangeCustomerStatusRequest("INACTIVE");

        ChangeCustomerStatusCommand cmd = mapper.toCommand(1L, request);

        assertThat(cmd.id()).isEqualTo(1L);
        assertThat(cmd.status()).isEqualTo("INACTIVE");
    }
}

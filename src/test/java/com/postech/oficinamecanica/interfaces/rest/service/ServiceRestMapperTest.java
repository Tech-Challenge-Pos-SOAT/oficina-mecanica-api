package com.postech.oficinamecanica.interfaces.rest.service;

import com.postech.oficinamecanica.application.service.ChangeServiceStatusCommand;
import com.postech.oficinamecanica.application.service.CreateServiceCommand;
import com.postech.oficinamecanica.application.service.UpdateServiceCommand;
import com.postech.oficinamecanica.domain.service.Service;
import com.postech.oficinamecanica.domain.shared.EntityStatus;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class ServiceRestMapperTest {
    private final ServiceRestMapper mapper = new ServiceRestMapperImpl();

    @Test
    void shouldMapCreateRequestToCommand() {
        CreateServiceCommand cmd = mapper.toCommand(
            new CreateServiceRequest("Troca de óleo", "desc", new BigDecimal("120.00"))
        );

        assertThat(cmd.name()).isEqualTo("Troca de óleo");
        assertThat(cmd.description()).isEqualTo("desc");
        assertThat(cmd.price()).isEqualByComparingTo("120.00");
    }

    @Test
    void shouldMapUpdateRequestToCommand() {
        UpdateServiceCommand cmd = mapper.toCommand(
            new UpdateServiceRequest("Troca de óleo sintético", "desc nova", new BigDecimal("135.00"))
        );

        assertThat(cmd.name()).isEqualTo("Troca de óleo sintético");
        assertThat(cmd.description()).isEqualTo("desc nova");
        assertThat(cmd.price()).isEqualByComparingTo("135.00");
    }

    @Test
    void shouldMapChangeStatusRequestToCommand() {
        ChangeServiceStatusCommand cmd = mapper.toCommand(new ChangeServiceStatusRequest("INACTIVE"));

        assertThat(cmd.status()).isEqualTo("INACTIVE");
    }

    @Test
    void shouldMapDomainToResponse() {
        Instant createdAt = Instant.now();
        Instant updatedAt = Instant.now();
        Service service = new Service(1L, "Troca de óleo", "desc", new BigDecimal("120.00"),
            EntityStatus.ACTIVE, createdAt, updatedAt);

        ServiceResponse response = mapper.toResponse(service);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("Troca de óleo");
        assertThat(response.description()).isEqualTo("desc");
        assertThat(response.price()).isEqualByComparingTo("120.00");
        assertThat(response.status()).isEqualTo("ACTIVE");
        assertThat(response.createdAt()).isEqualTo(createdAt);
        assertThat(response.updatedAt()).isEqualTo(updatedAt);
    }
}

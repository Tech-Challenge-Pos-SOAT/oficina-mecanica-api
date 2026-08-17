package com.postech.oficinamecanica.domain.service;

import com.postech.oficinamecanica.domain.shared.EntityStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ServiceTest {

    @Test
    void shouldCreateServiceWithValidPrice() {
        Instant createdAt = Instant.now();
        Instant updatedAt = Instant.now();
        Service service = new Service(
            null, "Troca de óleo", "Troca de óleo do motor",
            new BigDecimal("120.00"), EntityStatus.ACTIVE, createdAt, updatedAt
        );

        assertThat(service.getName()).isEqualTo("Troca de óleo");
        assertThat(service.getPrice()).isEqualByComparingTo("120.00");
        assertThat(service.getStatus()).isEqualTo(EntityStatus.ACTIVE);
        assertThat(service.getCreatedAt()).isEqualTo(createdAt);
        assertThat(service.getUpdatedAt()).isEqualTo(updatedAt);
    }

    @ParameterizedTest
    @ValueSource(strings = {"0.00", "-1.00"})
    void shouldRejectPriceZeroOrNegative(String price) {
        assertThatThrownBy(() -> new Service(
            null, "Troca de óleo", "desc",
            new BigDecimal(price), EntityStatus.ACTIVE, Instant.now(), Instant.now()
        )).isInstanceOf(InvalidServicePriceException.class);
    }

    @Test
    void shouldRejectNullPrice() {
        assertThatThrownBy(() -> new Service(
            null, "Troca de óleo", "desc",
            null, EntityStatus.ACTIVE, Instant.now(), Instant.now()
        )).isInstanceOf(InvalidServicePriceException.class);
    }

    @Test
    void shouldUpdateNameDescriptionAndPrice() {
        Service service = new Service(
            1L, "Troca de óleo", "desc antiga",
            new BigDecimal("120.00"), EntityStatus.ACTIVE, Instant.now(), Instant.now()
        );

        Service updated = service.update("Troca de óleo sintético", "desc nova", new BigDecimal("135.00"));

        assertThat(updated.getName()).isEqualTo("Troca de óleo sintético");
        assertThat(updated.getDescription()).isEqualTo("desc nova");
        assertThat(updated.getPrice()).isEqualByComparingTo("135.00");
        assertThat(updated.getStatus()).isEqualTo(EntityStatus.ACTIVE);
    }

    @Test
    void shouldDeactivateActiveService() {
        Service service = new Service(
            1L, "Troca de óleo", "desc",
            new BigDecimal("120.00"), EntityStatus.ACTIVE, Instant.now(), Instant.now()
        );

        Service deactivated = service.deactivate();

        assertThat(deactivated.getStatus()).isEqualTo(EntityStatus.INACTIVE);
    }

    @Test
    void shouldNotAllowDeactivatingAlreadyInactiveService() {
        Service service = new Service(
            1L, "Troca de óleo", "desc",
            new BigDecimal("120.00"), EntityStatus.INACTIVE, Instant.now(), Instant.now()
        );

        assertThatThrownBy(service::deactivate)
            .isInstanceOf(ServiceAlreadyInactiveException.class);
    }

    @Test
    void shouldActivateInactiveService() {
        Service service = new Service(
            1L, "Troca de óleo", "desc",
            new BigDecimal("120.00"), EntityStatus.INACTIVE, Instant.now(), Instant.now()
        );

        Service activated = service.activate();

        assertThat(activated.getStatus()).isEqualTo(EntityStatus.ACTIVE);
    }

    @Test
    void shouldNotAllowActivatingAlreadyActiveService() {
        Service service = new Service(
            1L, "Troca de óleo", "desc",
            new BigDecimal("120.00"), EntityStatus.ACTIVE, Instant.now(), Instant.now()
        );

        assertThatThrownBy(service::activate)
            .isInstanceOf(ServiceAlreadyActiveException.class);
    }
}

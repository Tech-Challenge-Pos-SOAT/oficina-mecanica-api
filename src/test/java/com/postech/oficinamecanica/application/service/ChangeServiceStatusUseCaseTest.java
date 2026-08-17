package com.postech.oficinamecanica.application.service;

import com.postech.oficinamecanica.domain.service.Service;
import com.postech.oficinamecanica.domain.service.ServiceAlreadyActiveException;
import com.postech.oficinamecanica.domain.service.ServiceNotFoundException;
import com.postech.oficinamecanica.domain.shared.EntityStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChangeServiceStatusUseCaseTest {
    @Mock
    private ServiceRepository repository;

    @InjectMocks
    private ChangeServiceStatusUseCase useCase;

    @Test
    void shouldDeactivateActiveService() {
        Service service = new Service(1L, "Troca de óleo", "desc", new BigDecimal("120.00"),
            EntityStatus.ACTIVE, Instant.now(), Instant.now());
        when(repository.findById(1L)).thenReturn(Optional.of(service));
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Service result = useCase.execute(1L, new ChangeServiceStatusCommand("INACTIVE"));

        assertThat(result.getStatus()).isEqualTo(EntityStatus.INACTIVE);
    }

    @Test
    void shouldActivateInactiveService() {
        Service service = new Service(1L, "Troca de óleo", "desc", new BigDecimal("120.00"),
            EntityStatus.INACTIVE, Instant.now(), Instant.now());
        when(repository.findById(1L)).thenReturn(Optional.of(service));
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Service result = useCase.execute(1L, new ChangeServiceStatusCommand("ACTIVE"));

        assertThat(result.getStatus()).isEqualTo(EntityStatus.ACTIVE);
    }

    @Test
    void shouldFailWhenReactivatingAlreadyActiveService() {
        Service service = new Service(1L, "Troca de óleo", "desc", new BigDecimal("120.00"),
            EntityStatus.ACTIVE, Instant.now(), Instant.now());
        when(repository.findById(1L)).thenReturn(Optional.of(service));

        assertThatThrownBy(() -> useCase.execute(1L, new ChangeServiceStatusCommand("active")))
            .isInstanceOf(ServiceAlreadyActiveException.class);
    }

    @Test
    void shouldFailWhenServiceNotFound() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(1L, new ChangeServiceStatusCommand("INACTIVE")))
            .isInstanceOf(ServiceNotFoundException.class);
    }

    @Test
    void shouldFailWhenStatusValueIsInvalid() {
        Service service = new Service(1L, "Troca de óleo", "desc", new BigDecimal("120.00"),
            EntityStatus.ACTIVE, Instant.now(), Instant.now());
        when(repository.findById(1L)).thenReturn(Optional.of(service));

        assertThatThrownBy(() -> useCase.execute(1L, new ChangeServiceStatusCommand("BLOCKED")))
            .isInstanceOf(IllegalArgumentException.class);
    }
}

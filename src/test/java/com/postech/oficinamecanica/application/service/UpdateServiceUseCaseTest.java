package com.postech.oficinamecanica.application.service;

import com.postech.oficinamecanica.domain.service.DuplicateServiceNameException;
import com.postech.oficinamecanica.domain.service.Service;
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
class UpdateServiceUseCaseTest {
    @Mock
    private ServiceRepository repository;

    @InjectMocks
    private UpdateServiceUseCase useCase;

    @Test
    void shouldUpdateServiceWhenNameIsUnchanged() {
        Service existing = new Service(1L, "Troca de óleo", "desc", new BigDecimal("120.00"),
            EntityStatus.ACTIVE, Instant.now(), Instant.now());
        UpdateServiceCommand cmd = new UpdateServiceCommand("Troca de óleo", "nova desc", new BigDecimal("135.00"));

        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.findByName("Troca de óleo")).thenReturn(Optional.of(existing));
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Service result = useCase.execute(1L, cmd);

        assertThat(result.getDescription()).isEqualTo("nova desc");
        assertThat(result.getPrice()).isEqualByComparingTo("135.00");
    }

    @Test
    void shouldFailWhenServiceNotFound() {
        UpdateServiceCommand cmd = new UpdateServiceCommand("Troca de óleo", "desc", new BigDecimal("135.00"));
        when(repository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(1L, cmd))
            .isInstanceOf(ServiceNotFoundException.class);
    }

    @Test
    void shouldFailWhenNewNameBelongsToAnotherService() {
        Service existing = new Service(1L, "Troca de óleo", "desc", new BigDecimal("120.00"),
            EntityStatus.ACTIVE, Instant.now(), Instant.now());
        Service other = new Service(2L, "Alinhamento", "desc", new BigDecimal("180.00"),
            EntityStatus.ACTIVE, Instant.now(), Instant.now());
        UpdateServiceCommand cmd = new UpdateServiceCommand("Alinhamento", "desc", new BigDecimal("135.00"));

        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.findByName("Alinhamento")).thenReturn(Optional.of(other));

        assertThatThrownBy(() -> useCase.execute(1L, cmd))
            .isInstanceOf(DuplicateServiceNameException.class);
    }
}

package com.postech.oficinamecanica.application.service;

import com.postech.oficinamecanica.domain.service.DuplicateServiceNameException;
import com.postech.oficinamecanica.domain.service.Service;
import com.postech.oficinamecanica.domain.shared.EntityStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateServiceUseCaseTest {
    @Mock
    private ServiceRepository repository;

    @InjectMocks
    private CreateServiceUseCase useCase;

    @Test
    void shouldCreateServiceWhenNameIsUnique() {
        CreateServiceCommand cmd = new CreateServiceCommand("Troca de óleo", "desc", new BigDecimal("120.00"));
        when(repository.findByName("Troca de óleo")).thenReturn(Optional.empty());
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Service result = useCase.execute(cmd);

        assertThat(result.getName()).isEqualTo("Troca de óleo");
        assertThat(result.getStatus()).isEqualTo(EntityStatus.ACTIVE);

        ArgumentCaptor<Service> captor = ArgumentCaptor.forClass(Service.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getId()).isNull();
    }

    @Test
    void shouldFailWhenNameAlreadyExists() {
        CreateServiceCommand cmd = new CreateServiceCommand("Troca de óleo", "desc", new BigDecimal("120.00"));
        Service existing = new Service(1L, "Troca de óleo", "desc", new BigDecimal("100.00"),
            EntityStatus.ACTIVE, Instant.now(), Instant.now());
        when(repository.findByName("Troca de óleo")).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> useCase.execute(cmd))
            .isInstanceOf(DuplicateServiceNameException.class);

        verify(repository, never()).save(any());
    }
}

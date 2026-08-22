package com.postech.oficinamecanica.application.service;

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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FindServiceByIdUseCaseTest {
    @Mock
    private ServiceRepository repository;

    @InjectMocks
    private FindServiceByIdUseCase useCase;

    @Test
    void shouldReturnServiceWhenFound() {
        Service service = new Service(1L, "Troca de óleo", "desc", new BigDecimal("120.00"),
            EntityStatus.ACTIVE, Instant.now(), Instant.now());
        when(repository.findById(1L)).thenReturn(Optional.of(service));

        Service result = useCase.execute(1L);

        assertThat(result.getName()).isEqualTo("Troca de óleo");
    }

    @Test
    void shouldFailWhenServiceNotFound() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(1L))
            .isInstanceOf(ServiceNotFoundException.class);
    }
}

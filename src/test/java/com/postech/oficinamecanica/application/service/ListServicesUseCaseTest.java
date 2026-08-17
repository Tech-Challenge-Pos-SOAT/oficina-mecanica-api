package com.postech.oficinamecanica.application.service;

import com.postech.oficinamecanica.domain.service.Service;
import com.postech.oficinamecanica.domain.shared.EntityStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListServicesUseCaseTest {
    @Mock
    private ServiceRepository repository;

    @InjectMocks
    private ListServicesUseCase useCase;

    @Test
    void shouldReturnAllServices() {
        Service service = new Service(1L, "Troca de óleo", "desc", new BigDecimal("120.00"),
            EntityStatus.ACTIVE, Instant.now(), Instant.now());
        when(repository.findAll()).thenReturn(List.of(service));

        List<Service> result = useCase.execute();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Troca de óleo");
    }

    @Test
    void shouldReturnEmptyListWhenNoServicesRegistered() {
        when(repository.findAll()).thenReturn(List.of());

        List<Service> result = useCase.execute();

        assertThat(result).isEmpty();
    }
}

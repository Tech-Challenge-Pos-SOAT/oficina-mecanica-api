package com.postech.oficinamecanica.application.vehicle;

import com.postech.oficinamecanica.domain.shared.EntityStatus;
import com.postech.oficinamecanica.domain.vehicle.Plate;
import com.postech.oficinamecanica.domain.vehicle.Vehicle;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListVehiclesUseCaseTest {
    @Mock
    private VehicleRepository repository;

    @InjectMocks
    private ListVehiclesUseCase useCase;

    @Test
    void shouldReturnActiveVehiclesWhenStatusParamIsNull() {
        Vehicle vehicle = aVehicle(EntityStatus.ACTIVE);
        when(repository.findByStatus(EntityStatus.ACTIVE)).thenReturn(List.of(vehicle));

        List<Vehicle> result = useCase.execute(null);

        assertThat(result).hasSize(1);
    }

    @Test
    void shouldReturnInactiveVehiclesWhenStatusIsInactive() {
        Vehicle vehicle = aVehicle(EntityStatus.INACTIVE);
        when(repository.findByStatus(EntityStatus.INACTIVE)).thenReturn(List.of(vehicle));

        List<Vehicle> result = useCase.execute("INACTIVE");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo(EntityStatus.INACTIVE);
    }

    @Test
    void shouldReturnEmptyListWhenNoVehiclesMatchStatus() {
        when(repository.findByStatus(EntityStatus.INACTIVE)).thenReturn(List.of());

        List<Vehicle> result = useCase.execute("inactive");

        assertThat(result).isEmpty();
    }

    private static Vehicle aVehicle(EntityStatus status) {
        return new Vehicle(1L, 1L, new Plate("ABC-1234"), "Toyota", "Corolla", 2021,
            status, Instant.now(), Instant.now());
    }
}

package com.postech.oficinamecanica.application.vehicle;

import com.postech.oficinamecanica.domain.shared.EntityStatus;
import com.postech.oficinamecanica.domain.vehicle.Plate;
import com.postech.oficinamecanica.domain.vehicle.Vehicle;
import com.postech.oficinamecanica.domain.vehicle.VehicleNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetVehicleUseCaseTest {
    @Mock
    private VehicleRepository repository;

    @InjectMocks
    private GetVehicleUseCase useCase;

    @Test
    void shouldReturnVehicleWhenFound() {
        Vehicle vehicle = new Vehicle(1L, 1L, new Plate("ABC-1234"), "Toyota", "Corolla", 2021,
            EntityStatus.ACTIVE, Instant.now(), Instant.now());
        when(repository.findById(1L)).thenReturn(Optional.of(vehicle));

        Vehicle result = useCase.execute(1L);

        assertThat(result.getBrand()).isEqualTo("Toyota");
    }

    @Test
    void shouldFailWhenVehicleNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(99L))
            .isInstanceOf(VehicleNotFoundException.class);
    }
}

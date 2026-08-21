package com.postech.oficinamecanica.application.vehicle;

import com.postech.oficinamecanica.domain.shared.EntityStatus;
import com.postech.oficinamecanica.domain.vehicle.Plate;
import com.postech.oficinamecanica.domain.vehicle.Vehicle;
import com.postech.oficinamecanica.domain.vehicle.VehicleAlreadyActiveException;
import com.postech.oficinamecanica.domain.vehicle.VehicleAlreadyInactiveException;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChangeVehicleStatusUseCaseTest {
    @Mock
    private VehicleRepository repository;

    @InjectMocks
    private ChangeVehicleStatusUseCase useCase;

    @Test
    void shouldDeactivateActiveVehicle() {
        Vehicle vehicle = aVehicle(EntityStatus.ACTIVE);
        when(repository.findById(1L)).thenReturn(Optional.of(vehicle));
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Vehicle result = useCase.execute(new ChangeVehicleStatusCommand(1L, "INACTIVE"));

        assertThat(result.getStatus()).isEqualTo(EntityStatus.INACTIVE);
    }

    @Test
    void shouldActivateInactiveVehicle() {
        Vehicle vehicle = aVehicle(EntityStatus.INACTIVE);
        when(repository.findById(1L)).thenReturn(Optional.of(vehicle));
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Vehicle result = useCase.execute(new ChangeVehicleStatusCommand(1L, "active"));

        assertThat(result.getStatus()).isEqualTo(EntityStatus.ACTIVE);
    }

    @Test
    void shouldFailWhenReactivatingVehicleThatIsAlreadyActive() {
        Vehicle vehicle = aVehicle(EntityStatus.ACTIVE);
        when(repository.findById(1L)).thenReturn(Optional.of(vehicle));

        assertThatThrownBy(() -> useCase.execute(new ChangeVehicleStatusCommand(1L, "ACTIVE")))
            .isInstanceOf(VehicleAlreadyActiveException.class);
    }

    @Test
    void shouldFailWhenDeactivatingVehicleThatIsAlreadyInactive() {
        Vehicle vehicle = aVehicle(EntityStatus.INACTIVE);
        when(repository.findById(1L)).thenReturn(Optional.of(vehicle));

        assertThatThrownBy(() -> useCase.execute(new ChangeVehicleStatusCommand(1L, "INACTIVE")))
            .isInstanceOf(VehicleAlreadyInactiveException.class);
    }

    @Test
    void shouldFailWhenVehicleNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(new ChangeVehicleStatusCommand(99L, "ACTIVE")))
            .isInstanceOf(VehicleNotFoundException.class);
    }

    private static Vehicle aVehicle(EntityStatus status) {
        return new Vehicle(1L, 1L, new Plate("ABC-1234"), "Toyota", "Corolla", 2021,
            status, Instant.now(), Instant.now());
    }
}

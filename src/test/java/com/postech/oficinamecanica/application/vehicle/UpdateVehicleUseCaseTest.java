package com.postech.oficinamecanica.application.vehicle;

import com.postech.oficinamecanica.domain.shared.EntityStatus;
import com.postech.oficinamecanica.domain.vehicle.DuplicatePlateException;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateVehicleUseCaseTest {
    @Mock
    private VehicleRepository repository;

    @InjectMocks
    private UpdateVehicleUseCase useCase;

    @Test
    void shouldUpdateVehicleDetailsWhenFound() {
        Vehicle vehicle = aVehicle(1L, "ABC-1234", "Toyota");
        UpdateVehicleCommand cmd = new UpdateVehicleCommand(1L, "Honda", "Civic", "XYZ-9876", 2022);

        when(repository.findById(1L)).thenReturn(Optional.of(vehicle));
        when(repository.findByPlate(new Plate("XYZ-9876"))).thenReturn(Optional.empty());
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Vehicle result = useCase.execute(cmd);

        assertThat(result.getBrand()).isEqualTo("Honda");
        assertThat(result.getPlate()).isEqualTo(new Plate("XYZ-9876"));
    }

    @Test
    void shouldFailWhenVehicleNotFound() {
        UpdateVehicleCommand cmd = new UpdateVehicleCommand(99L, "Honda", "Civic", "XYZ-9876", 2022);
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(cmd))
            .isInstanceOf(VehicleNotFoundException.class);
    }

    @Test
    void shouldFailWhenPlateBelongsToAnotherVehicle() {
        Vehicle vehicle = aVehicle(1L, "ABC-1234", "Toyota");
        Vehicle other = aVehicle(2L, "XYZ-9876", "Honda");
        UpdateVehicleCommand cmd = new UpdateVehicleCommand(1L, "Toyota", "Corolla", "XYZ-9876", 2021);

        when(repository.findById(1L)).thenReturn(Optional.of(vehicle));
        when(repository.findByPlate(new Plate("XYZ-9876"))).thenReturn(Optional.of(other));

        assertThatThrownBy(() -> useCase.execute(cmd))
            .isInstanceOf(DuplicatePlateException.class);
    }

    @Test
    void shouldAllowKeepingOwnPlateUnchanged() {
        Vehicle vehicle = aVehicle(1L, "ABC-1234", "Toyota");
        UpdateVehicleCommand cmd = new UpdateVehicleCommand(1L, "Toyota", "Corolla", "ABC-1234", 2021);

        when(repository.findById(1L)).thenReturn(Optional.of(vehicle));
        when(repository.findByPlate(new Plate("ABC-1234"))).thenReturn(Optional.of(vehicle));
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Vehicle result = useCase.execute(cmd);

        assertThat(result.getPlate()).isEqualTo(new Plate("ABC-1234"));
    }

    private static Vehicle aVehicle(Long id, String plate, String brand) {
        return new Vehicle(id, 1L, new Plate(plate), brand, "Model", 2020,
            EntityStatus.ACTIVE, Instant.now(), Instant.now());
    }
}

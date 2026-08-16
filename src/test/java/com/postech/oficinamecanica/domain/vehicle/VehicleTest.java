package com.postech.oficinamecanica.domain.vehicle;

import com.postech.oficinamecanica.domain.shared.EntityStatus;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VehicleTest {

    @Test
    void shouldCreateVehicleWithActiveStatus() {
        Vehicle vehicle = Vehicle.create(1L, new Plate("ABC-1234"), "Toyota", "Corolla", 2021);

        assertThat(vehicle.getStatus()).isEqualTo(EntityStatus.ACTIVE);
        assertThat(vehicle.getBrand()).isEqualTo("Toyota");
        assertThat(vehicle.getId()).isNull();
    }

    @Test
    void shouldDeactivateActiveVehicle() {
        Vehicle vehicle = Vehicle.create(1L, new Plate("ABC-1234"), "Toyota", "Corolla", 2021);

        vehicle.deactivate();

        assertThat(vehicle.getStatus()).isEqualTo(EntityStatus.INACTIVE);
    }

    @Test
    void shouldNotAllowDeactivatingTwice() {
        Vehicle vehicle = Vehicle.create(1L, new Plate("ABC-1234"), "Toyota", "Corolla", 2021);
        vehicle.deactivate();

        assertThatThrownBy(vehicle::deactivate)
            .isInstanceOf(VehicleAlreadyInactiveException.class);
    }

    @Test
    void shouldActivateInactiveVehicle() {
        Vehicle vehicle = Vehicle.create(1L, new Plate("ABC-1234"), "Toyota", "Corolla", 2021);
        vehicle.deactivate();

        vehicle.activate();

        assertThat(vehicle.getStatus()).isEqualTo(EntityStatus.ACTIVE);
    }

    @Test
    void shouldNotAllowReactivatingVehicleThatIsAlreadyActive() {
        Vehicle vehicle = Vehicle.create(1L, new Plate("ABC-1234"), "Toyota", "Corolla", 2021);

        assertThatThrownBy(vehicle::activate)
            .isInstanceOf(VehicleAlreadyActiveException.class);
    }

    @Test
    void shouldUpdateDetails() {
        Vehicle vehicle = Vehicle.create(1L, new Plate("ABC-1234"), "Toyota", "Corolla", 2021);

        vehicle.updateDetails(new Plate("XYZ-9876"), "Honda", "Civic", 2022);

        assertThat(vehicle.getPlate()).isEqualTo(new Plate("XYZ-9876"));
        assertThat(vehicle.getBrand()).isEqualTo("Honda");
        assertThat(vehicle.getModel()).isEqualTo("Civic");
        assertThat(vehicle.getYear()).isEqualTo(2022);
    }
}

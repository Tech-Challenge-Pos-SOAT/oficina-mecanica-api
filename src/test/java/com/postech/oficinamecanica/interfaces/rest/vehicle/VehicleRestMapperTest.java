package com.postech.oficinamecanica.interfaces.rest.vehicle;

import com.postech.oficinamecanica.application.vehicle.ChangeVehicleStatusCommand;
import com.postech.oficinamecanica.application.vehicle.CreateVehicleCommand;
import com.postech.oficinamecanica.application.vehicle.UpdateVehicleCommand;
import com.postech.oficinamecanica.domain.shared.EntityStatus;
import com.postech.oficinamecanica.domain.vehicle.Plate;
import com.postech.oficinamecanica.domain.vehicle.Vehicle;
import org.junit.jupiter.api.Test;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class VehicleRestMapperTest {
    private final VehicleRestMapper mapper = new VehicleRestMapperImpl();

    @Test
    void shouldMapVehicleToResponse() {
        Vehicle vehicle = new Vehicle(
            1L, 2L, new Plate("ABC-1234"), "Toyota", "Corolla", 2021,
            EntityStatus.ACTIVE, Instant.now(), Instant.now()
        );

        VehicleResponse response = mapper.toResponse(vehicle);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.customerId()).isEqualTo(2L);
        assertThat(response.plate()).isEqualTo("ABC-1234");
        assertThat(response.status()).isEqualTo("ACTIVE");
    }

    @Test
    void shouldMapCreateRequestToCommand() {
        CreateVehicleRequest request = new CreateVehicleRequest(2L, "Toyota", "Corolla", "ABC-1234", 2021);

        CreateVehicleCommand cmd = mapper.toCommand(request);

        assertThat(cmd.customerId()).isEqualTo(2L);
        assertThat(cmd.brand()).isEqualTo("Toyota");
    }

    @Test
    void shouldMapUpdateRequestToCommandWithId() {
        UpdateVehicleRequest request = new UpdateVehicleRequest("Honda", "Civic", "XYZ-9876", 2022);

        UpdateVehicleCommand cmd = mapper.toCommand(1L, request);

        assertThat(cmd.id()).isEqualTo(1L);
        assertThat(cmd.brand()).isEqualTo("Honda");
    }

    @Test
    void shouldMapChangeStatusRequestToCommandWithId() {
        ChangeVehicleStatusRequest request = new ChangeVehicleStatusRequest("INACTIVE");

        ChangeVehicleStatusCommand cmd = mapper.toCommand(1L, request);

        assertThat(cmd.id()).isEqualTo(1L);
        assertThat(cmd.status()).isEqualTo("INACTIVE");
    }
}

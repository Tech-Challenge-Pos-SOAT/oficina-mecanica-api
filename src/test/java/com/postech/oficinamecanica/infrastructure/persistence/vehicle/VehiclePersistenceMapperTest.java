package com.postech.oficinamecanica.infrastructure.persistence.vehicle;

import com.postech.oficinamecanica.domain.shared.EntityStatus;
import com.postech.oficinamecanica.domain.vehicle.Plate;
import com.postech.oficinamecanica.domain.vehicle.Vehicle;
import org.junit.jupiter.api.Test;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class VehiclePersistenceMapperTest {
    private final VehiclePersistenceMapper mapper = new VehiclePersistenceMapperImpl();

    @Test
    void shouldMapEntityToDomain() {
        VehicleJpaEntity entity = new VehicleJpaEntity(
            1L, 2L, "Toyota", "Corolla", "ABC-1234", 2021,
            EntityStatus.ACTIVE, Instant.now(), Instant.now()
        );

        Vehicle domain = mapper.toDomain(entity);

        assertThat(domain.getPlate()).isEqualTo(new Plate("ABC-1234"));
        assertThat(domain.getBrand()).isEqualTo("Toyota");
        assertThat(domain.getCustomerId()).isEqualTo(2L);
    }

    @Test
    void shouldMapDomainToEntity() {
        Vehicle domain = new Vehicle(
            1L, 2L, new Plate("ABC-1234"), "Toyota", "Corolla", 2021,
            EntityStatus.ACTIVE, Instant.now(), Instant.now()
        );

        VehicleJpaEntity entity = mapper.toPersistence(domain);

        assertThat(entity.getPlate()).isEqualTo("ABC-1234");
        assertThat(entity.getBrand()).isEqualTo("Toyota");
        assertThat(entity.getCustomerId()).isEqualTo(2L);
    }

    @Test
    void shouldReturnNullWhenMappingNullPlateValue() {
        assertThat(mapper.map((String) null)).isNull();
        assertThat(mapper.map((Plate) null)).isNull();
    }
}

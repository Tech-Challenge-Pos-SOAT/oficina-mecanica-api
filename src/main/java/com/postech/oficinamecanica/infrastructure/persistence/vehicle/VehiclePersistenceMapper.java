package com.postech.oficinamecanica.infrastructure.persistence.vehicle;

import com.postech.oficinamecanica.domain.vehicle.Plate;
import com.postech.oficinamecanica.domain.vehicle.Vehicle;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface VehiclePersistenceMapper {

    @Mapping(target = "plate", source = "plate")
    Vehicle toDomain(VehicleJpaEntity entity);

    @Mapping(target = "plate", source = "plate.value")
    VehicleJpaEntity toPersistence(Vehicle domain);

    default Plate map(String value) {
        return value == null ? null : new Plate(value);
    }

    default String map(Plate plate) {
        return plate == null ? null : plate.value();
    }
}

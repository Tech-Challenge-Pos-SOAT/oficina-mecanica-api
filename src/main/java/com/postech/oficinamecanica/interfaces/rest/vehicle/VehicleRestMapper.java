package com.postech.oficinamecanica.interfaces.rest.vehicle;

import com.postech.oficinamecanica.application.vehicle.ChangeVehicleStatusCommand;
import com.postech.oficinamecanica.application.vehicle.CreateVehicleCommand;
import com.postech.oficinamecanica.application.vehicle.UpdateVehicleCommand;
import com.postech.oficinamecanica.domain.vehicle.Vehicle;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface VehicleRestMapper {

    @Mapping(target = "plate", source = "plate.value")
    @Mapping(target = "status", expression = "java(domain.getStatus().name())")
    VehicleResponse toResponse(Vehicle domain);

    CreateVehicleCommand toCommand(CreateVehicleRequest request);

    @Mapping(target = "id", source = "id")
    UpdateVehicleCommand toCommand(Long id, UpdateVehicleRequest request);

    @Mapping(target = "id", source = "id")
    ChangeVehicleStatusCommand toCommand(Long id, ChangeVehicleStatusRequest request);
}

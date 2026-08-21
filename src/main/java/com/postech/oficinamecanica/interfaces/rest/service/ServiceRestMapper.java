package com.postech.oficinamecanica.interfaces.rest.service;

import com.postech.oficinamecanica.application.service.ChangeServiceStatusCommand;
import com.postech.oficinamecanica.application.service.CreateServiceCommand;
import com.postech.oficinamecanica.application.service.UpdateServiceCommand;
import com.postech.oficinamecanica.domain.service.Service;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface ServiceRestMapper {

    CreateServiceCommand toCommand(CreateServiceRequest request);

    UpdateServiceCommand toCommand(UpdateServiceRequest request);

    ChangeServiceStatusCommand toCommand(ChangeServiceStatusRequest request);

    @Mapping(target = "status", expression = "java(domain.getStatus().name())")
    ServiceResponse toResponse(Service domain);
}

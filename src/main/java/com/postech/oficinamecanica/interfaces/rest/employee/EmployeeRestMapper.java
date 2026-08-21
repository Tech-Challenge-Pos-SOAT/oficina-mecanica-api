package com.postech.oficinamecanica.interfaces.rest.employee;

import com.postech.oficinamecanica.application.employee.CreateEmployeeCommand;
import com.postech.oficinamecanica.application.employee.UpdateEmployeeCommand;
import com.postech.oficinamecanica.domain.employee.Employee;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface EmployeeRestMapper {

    CreateEmployeeCommand toCommand(CreateEmployeeRequest request);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "request.name")
    @Mapping(target = "email", source = "request.email")
    @Mapping(target = "role", source = "request.role")
    UpdateEmployeeCommand toCommand(Long id, UpdateEmployeeRequest request);

    @Mapping(target = "role", expression = "java(domain.getRole().name())")
    @Mapping(target = "status", expression = "java(domain.getStatus().name())")
    EmployeeResponse toResponse(Employee domain);
}

package com.postech.oficinamecanica.interfaces.rest.customer;

import com.postech.oficinamecanica.application.customer.ChangeCustomerStatusCommand;
import com.postech.oficinamecanica.application.customer.CreateCustomerCommand;
import com.postech.oficinamecanica.application.customer.UpdateCustomerCommand;
import com.postech.oficinamecanica.domain.customer.Customer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface CustomerRestMapper {

    @Mapping(target = "document", source = "document.value")
    @Mapping(target = "status", expression = "java(domain.getStatus().name())")
    CustomerResponse toResponse(Customer domain);

    CreateCustomerCommand toCommand(CreateCustomerRequest request);

    @Mapping(target = "id", source = "id")
    UpdateCustomerCommand toCommand(Long id, UpdateCustomerRequest request);

    @Mapping(target = "id", source = "id")
    ChangeCustomerStatusCommand toCommand(Long id, ChangeCustomerStatusRequest request);
}

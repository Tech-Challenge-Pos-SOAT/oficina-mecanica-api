package com.postech.oficinamecanica.interfaces.rest.materialtransaction;

import com.postech.oficinamecanica.domain.materialtransaction.MaterialTransaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface MaterialTransactionRestMapper {

    @Mapping(target = "id", source = "id")
    @Mapping(target = "materialId", source = "materialId")
    @Mapping(target = "serviceOrderId", source = "serviceOrderId")
    @Mapping(target = "quantity", source = "quantity")
    @Mapping(target = "type", expression = "java(domain.getType().name())")
    @Mapping(target = "createdAt", source = "createdAt")
    MaterialTransactionResponse toResponse(MaterialTransaction domain);
}

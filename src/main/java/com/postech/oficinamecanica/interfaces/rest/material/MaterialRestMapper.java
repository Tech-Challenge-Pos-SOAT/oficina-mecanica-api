package com.postech.oficinamecanica.interfaces.rest.material;

import com.postech.oficinamecanica.domain.material.Material;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface MaterialRestMapper {

    @Mapping(target = "status", expression = "java(domain.getStatus().name())")
    MaterialResponse toResponse(Material domain);
}

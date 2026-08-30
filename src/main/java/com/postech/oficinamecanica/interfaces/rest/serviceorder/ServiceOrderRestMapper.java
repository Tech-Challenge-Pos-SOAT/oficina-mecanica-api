package com.postech.oficinamecanica.interfaces.rest.serviceorder;

import com.postech.oficinamecanica.domain.serviceorder.ServiceOrder;
import com.postech.oficinamecanica.domain.serviceorder.ServiceOrderHistory;
import com.postech.oficinamecanica.domain.serviceorder.ServiceOrderMaterial;
import com.postech.oficinamecanica.domain.serviceorder.ServiceOrderService;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface ServiceOrderRestMapper {

    @Mapping(target = "status", expression = "java(domain.getStatus().name())")
    ServiceOrderResponse toResponse(ServiceOrder domain);

    @Mapping(target = "status", expression = "java(domain.getStatus().name())")
    ServiceOrderTrackingResponse toTrackingResponse(ServiceOrder domain);

    ServiceOrderServiceResponse toResponse(ServiceOrderService domain);

    @Mapping(target = "total", expression = "java(domain.total())")
    ServiceOrderMaterialResponse toResponse(ServiceOrderMaterial domain);

    @Mapping(target = "status", expression = "java(domain.getStatus().name())")
    @Mapping(target = "authorType", expression = "java(domain.getAuthorType().name())")
    ServiceOrderHistoryResponse toResponse(ServiceOrderHistory domain);
}

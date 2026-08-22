package com.postech.oficinamecanica.infrastructure.persistence.service;

import com.postech.oficinamecanica.domain.service.Service;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface ServicePersistenceMapper {
    Service toDomain(ServiceJpaEntity entity);
    ServiceJpaEntity toPersistence(Service domain);
}

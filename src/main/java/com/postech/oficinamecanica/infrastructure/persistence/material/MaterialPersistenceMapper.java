package com.postech.oficinamecanica.infrastructure.persistence.material;

import com.postech.oficinamecanica.domain.material.Material;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface MaterialPersistenceMapper {

    Material toDomain(MaterialJpaEntity entity);
    MaterialJpaEntity toPersistence(Material domain);
}

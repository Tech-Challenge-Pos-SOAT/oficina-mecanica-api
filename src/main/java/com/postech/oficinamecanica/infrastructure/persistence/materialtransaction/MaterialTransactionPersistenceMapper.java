package com.postech.oficinamecanica.infrastructure.persistence.materialtransaction;

import com.postech.oficinamecanica.domain.materialtransaction.MaterialTransaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface MaterialTransactionPersistenceMapper {

    @Mapping(target = "id", source = "id")
    @Mapping(target = "materialId", source = "materialId")
    @Mapping(target = "serviceOrderId", source = "serviceOrderId")
    @Mapping(target = "quantity", source = "quantity")
    @Mapping(target = "type", source = "type")
    @Mapping(target = "createdAt", source = "createdAt")
    MaterialTransaction toDomain(MaterialTransactionJpaEntity entity);

    default MaterialTransactionJpaEntity toPersistence(MaterialTransaction domain) {
        return new MaterialTransactionJpaEntity(
                domain.getId(),
                domain.getMaterialId(),
                domain.getServiceOrderId(),
                domain.getQuantity(),
                domain.getType(),
                domain.getCreatedAt()
        );
    }
}

package com.postech.oficinamecanica.infrastructure.persistence.customer;

import com.postech.oficinamecanica.domain.customer.Customer;
import com.postech.oficinamecanica.domain.customer.Document;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface CustomerPersistenceMapper {

    @Mapping(target = "document", source = "document")
    Customer toDomain(CustomerJpaEntity entity);

    @Mapping(target = "document", source = "document.value")
    CustomerJpaEntity toPersistence(Customer domain);

    default Document map(String value) {
        return value == null ? null : new Document(value);
    }

    default String map(Document document) {
        return document == null ? null : document.value();
    }
}

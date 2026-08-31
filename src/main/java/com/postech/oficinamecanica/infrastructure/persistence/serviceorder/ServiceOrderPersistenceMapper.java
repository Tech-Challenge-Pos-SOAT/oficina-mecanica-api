package com.postech.oficinamecanica.infrastructure.persistence.serviceorder;

import com.postech.oficinamecanica.domain.serviceorder.ServiceOrder;
import com.postech.oficinamecanica.domain.serviceorder.ServiceOrderHistory;
import com.postech.oficinamecanica.domain.serviceorder.ServiceOrderMaterial;
import com.postech.oficinamecanica.domain.serviceorder.ServiceOrderService;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.ArrayList;
import java.util.List;

/**
 * Itens e historico sao convertidos pelo MapStruct (campos 1:1). A raiz do
 * agregado e montada aqui a mao de proposito: a entidade JPA e bidirecional
 * (filho conhece o pai) e mapper gerado nessa forma entra em recursao.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface ServiceOrderPersistenceMapper {

    ServiceOrderService toDomain(ServiceOrderServiceJpaEntity entity);

    ServiceOrderMaterial toDomain(ServiceOrderMaterialJpaEntity entity);

    ServiceOrderHistory toDomain(ServiceOrderHistoryJpaEntity entity);

    default ServiceOrder toDomain(ServiceOrderJpaEntity entity) {
        if (entity == null) {
            return null;
        }

        List<ServiceOrderService> services = new ArrayList<>();
        for (ServiceOrderServiceJpaEntity service : entity.getServices()) {
            services.add(toDomain(service));
        }

        List<ServiceOrderMaterial> materials = new ArrayList<>();
        for (ServiceOrderMaterialJpaEntity material : entity.getMaterials()) {
            materials.add(toDomain(material));
        }

        List<ServiceOrderHistory> history = new ArrayList<>();
        for (ServiceOrderHistoryJpaEntity entry : entity.getHistory()) {
            history.add(toDomain(entry));
        }

        return new ServiceOrder(
            entity.getId(),
            entity.getCustomerId(),
            entity.getVehicleId(),
            entity.getPrice(),
            entity.getStatus(),
            services,
            materials,
            history,
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    default ServiceOrderJpaEntity toPersistence(ServiceOrder domain) {
        ServiceOrderJpaEntity entity = new ServiceOrderJpaEntity(
            domain.getId(),
            domain.getCustomerId(),
            domain.getVehicleId(),
            domain.getPrice(),
            domain.getStatus(),
            domain.getCreatedAt(),
            domain.getUpdatedAt()
        );

        for (ServiceOrderService service : domain.getServices()) {
            entity.addService(new ServiceOrderServiceJpaEntity(
                service.getId(),
                service.getServiceId(),
                service.getPrice(),
                service.isApproved(),
                service.getCreatedAt(),
                service.getUpdatedAt()
            ));
        }

        for (ServiceOrderMaterial material : domain.getMaterials()) {
            entity.addMaterial(new ServiceOrderMaterialJpaEntity(
                material.getId(),
                material.getMaterialId(),
                material.getQuantity(),
                material.getPrice(),
                material.isStockDebited(),
                material.getCreatedAt(),
                material.getUpdatedAt()
            ));
        }

        for (ServiceOrderHistory entry : domain.getHistory()) {
            entity.addHistory(new ServiceOrderHistoryJpaEntity(
                entry.getId(),
                entry.getStatus(),
                entry.getPrice(),
                entry.getAuthorType(),
                entry.getAuthorId(),
                entry.getObservation(),
                entry.getCreatedAt()
            ));
        }

        return entity;
    }
}

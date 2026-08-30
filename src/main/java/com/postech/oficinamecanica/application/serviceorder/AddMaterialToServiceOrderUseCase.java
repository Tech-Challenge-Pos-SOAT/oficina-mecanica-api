package com.postech.oficinamecanica.application.serviceorder;

import com.postech.oficinamecanica.application.material.MaterialRepository;
import com.postech.oficinamecanica.domain.material.InactiveMaterialException;
import com.postech.oficinamecanica.domain.material.Material;
import com.postech.oficinamecanica.domain.serviceorder.ServiceOrder;
import com.postech.oficinamecanica.domain.serviceorder.ServiceOrderNotFoundException;
import com.postech.oficinamecanica.domain.shared.EntityStatus;
import com.postech.oficinamecanica.domain.shared.exceptions.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Inclusao de peca/insumo no orcamento. Nao mexe em estoque: a baixa so
 * acontece na aprovacao do cliente.
 */
@Service
public class AddMaterialToServiceOrderUseCase {
    private final ServiceOrderRepository serviceOrderRepository;
    private final MaterialRepository materialRepository;
    private final ActiveEmployeeFinder activeEmployeeFinder;

    public AddMaterialToServiceOrderUseCase(ServiceOrderRepository serviceOrderRepository,
                                            MaterialRepository materialRepository,
                                            ActiveEmployeeFinder activeEmployeeFinder) {
        this.serviceOrderRepository = serviceOrderRepository;
        this.materialRepository = materialRepository;
        this.activeEmployeeFinder = activeEmployeeFinder;
    }

    @Transactional
    public ServiceOrder execute(Long serviceOrderId, Long materialId, Integer quantity, Long employeeId) {
        activeEmployeeFinder.findActive(employeeId);

        ServiceOrder order = serviceOrderRepository.findById(serviceOrderId)
            .orElseThrow(() -> new ServiceOrderNotFoundException(serviceOrderId));

        Material material = materialRepository.findById(materialId)
            .orElseThrow(() -> new ResourceNotFoundException("Material", materialId));

        if (material.getStatus() != EntityStatus.ACTIVE) {
            throw new InactiveMaterialException("Cannot add inactive material " + materialId + " to a service order");
        }

        order.addMaterial(material.getId(), quantity, material.getPrice());
        return serviceOrderRepository.save(order);
    }
}

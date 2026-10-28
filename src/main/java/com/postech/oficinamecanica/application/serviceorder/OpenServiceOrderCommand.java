package com.postech.oficinamecanica.application.serviceorder;

import java.util.List;

/**
 * Dados de abertura da OS. {@code serviceIds} e {@code materials} sao
 * opcionais: quando informados, entram na ordem ja na abertura (mesma regra
 * dos endpoints /services e /materials); nulos viram listas vazias.
 */
public record OpenServiceOrderCommand(
    String customerDocument,
    String plate,
    Long employeeId,
    List<Long> serviceIds,
    List<MaterialItem> materials
) {
    public OpenServiceOrderCommand {
        serviceIds = serviceIds == null ? List.of() : List.copyOf(serviceIds);
        materials = materials == null ? List.of() : List.copyOf(materials);
    }

    public OpenServiceOrderCommand(String customerDocument, String plate, Long employeeId) {
        this(customerDocument, plate, employeeId, List.of(), List.of());
    }

    public record MaterialItem(Long materialId, Integer quantity) {}
}

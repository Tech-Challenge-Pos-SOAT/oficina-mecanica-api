package com.postech.oficinamecanica.interfaces.rest.serviceorder;

import com.postech.oficinamecanica.application.serviceorder.ApproveServiceOrderBudgetUseCase;
import com.postech.oficinamecanica.application.serviceorder.GetCustomerServiceOrderUseCase;
import com.postech.oficinamecanica.application.serviceorder.RejectServiceOrderBudgetUseCase;
import com.postech.oficinamecanica.application.serviceorder.TrackServiceOrdersByDocumentUseCase;
import com.postech.oficinamecanica.domain.serviceorder.ServiceOrder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Acompanhamento e aprovacao pelo cliente. Fica fora de "/api/*" de proposito:
 * o cliente nao tem login de funcionario. O CPF/CNPJ informado precisa bater
 * com o dono da ordem, senao a resposta e 403.
 */
@RestController
@RequestMapping("/public/service-orders")
@Validated
@Tag(name = "Acompanhamento do cliente", description = "Consulta e aprovacao de orcamento pelo cliente")
public class PublicServiceOrderController {
    private final TrackServiceOrdersByDocumentUseCase trackServiceOrdersUseCase;
    private final GetCustomerServiceOrderUseCase getCustomerServiceOrderUseCase;
    private final ApproveServiceOrderBudgetUseCase approveBudgetUseCase;
    private final RejectServiceOrderBudgetUseCase rejectBudgetUseCase;
    private final ServiceOrderRestMapper mapper;

    public PublicServiceOrderController(TrackServiceOrdersByDocumentUseCase trackServiceOrdersUseCase,
                                        GetCustomerServiceOrderUseCase getCustomerServiceOrderUseCase,
                                        ApproveServiceOrderBudgetUseCase approveBudgetUseCase,
                                        RejectServiceOrderBudgetUseCase rejectBudgetUseCase,
                                        ServiceOrderRestMapper mapper) {
        this.trackServiceOrdersUseCase = trackServiceOrdersUseCase;
        this.getCustomerServiceOrderUseCase = getCustomerServiceOrderUseCase;
        this.approveBudgetUseCase = approveBudgetUseCase;
        this.rejectBudgetUseCase = rejectBudgetUseCase;
        this.mapper = mapper;
    }

    @GetMapping
    @Operation(summary = "Lista as ordens do cliente pelo CPF/CNPJ")
    public List<ServiceOrderTrackingResponse> track(@RequestParam @NotBlank String document) {
        return trackServiceOrdersUseCase.execute(document)
            .stream()
            .map(mapper::toTrackingResponse)
            .toList();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Detalha uma ordem do cliente, com orcamento e historico")
    public ServiceOrderResponse findById(@PathVariable Long id, @RequestParam @NotBlank String document) {
        return mapper.toResponse(getCustomerServiceOrderUseCase.execute(id, document));
    }

    @PostMapping("/{id}/approval")
    @Operation(summary = "Aprova ou recusa o orcamento; a aprovacao da baixa no estoque")
    public ServiceOrderResponse decide(@PathVariable Long id, @Valid @RequestBody BudgetDecisionRequest request) {
        ServiceOrder order = Boolean.TRUE.equals(request.approved())
            ? approveBudgetUseCase.execute(id, request.customerDocument())
            : rejectBudgetUseCase.execute(id, request.customerDocument(), request.reason());

        return mapper.toResponse(order);
    }
}

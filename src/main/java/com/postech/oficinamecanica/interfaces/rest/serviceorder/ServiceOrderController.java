package com.postech.oficinamecanica.interfaces.rest.serviceorder;

import com.postech.oficinamecanica.application.serviceorder.AddMaterialToServiceOrderUseCase;
import com.postech.oficinamecanica.application.serviceorder.AddServiceToServiceOrderUseCase;
import com.postech.oficinamecanica.application.serviceorder.DeliverServiceOrderUseCase;
import com.postech.oficinamecanica.application.serviceorder.FinishServiceOrderUseCase;
import com.postech.oficinamecanica.application.serviceorder.GetServiceOrderUseCase;
import com.postech.oficinamecanica.application.serviceorder.ListServiceOrdersUseCase;
import com.postech.oficinamecanica.application.serviceorder.OpenServiceOrderCommand;
import com.postech.oficinamecanica.application.serviceorder.OpenServiceOrderUseCase;
import com.postech.oficinamecanica.application.serviceorder.StartServiceOrderDiagnosisUseCase;
import com.postech.oficinamecanica.application.serviceorder.SubmitServiceOrderBudgetUseCase;
import com.postech.oficinamecanica.domain.serviceorder.ServiceOrder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/service-orders")
@Tag(name = "Service Orders", description = "Ciclo de vida da ordem de servico (uso interno da oficina)")
public class ServiceOrderController {
    private final OpenServiceOrderUseCase openServiceOrderUseCase;
    private final StartServiceOrderDiagnosisUseCase startDiagnosisUseCase;
    private final AddServiceToServiceOrderUseCase addServiceUseCase;
    private final AddMaterialToServiceOrderUseCase addMaterialUseCase;
    private final SubmitServiceOrderBudgetUseCase submitBudgetUseCase;
    private final FinishServiceOrderUseCase finishServiceOrderUseCase;
    private final DeliverServiceOrderUseCase deliverServiceOrderUseCase;
    private final GetServiceOrderUseCase getServiceOrderUseCase;
    private final ListServiceOrdersUseCase listServiceOrdersUseCase;
    private final ServiceOrderRestMapper mapper;

    public ServiceOrderController(OpenServiceOrderUseCase openServiceOrderUseCase,
                                  StartServiceOrderDiagnosisUseCase startDiagnosisUseCase,
                                  AddServiceToServiceOrderUseCase addServiceUseCase,
                                  AddMaterialToServiceOrderUseCase addMaterialUseCase,
                                  SubmitServiceOrderBudgetUseCase submitBudgetUseCase,
                                  FinishServiceOrderUseCase finishServiceOrderUseCase,
                                  DeliverServiceOrderUseCase deliverServiceOrderUseCase,
                                  GetServiceOrderUseCase getServiceOrderUseCase,
                                  ListServiceOrdersUseCase listServiceOrdersUseCase,
                                  ServiceOrderRestMapper mapper) {
        this.openServiceOrderUseCase = openServiceOrderUseCase;
        this.startDiagnosisUseCase = startDiagnosisUseCase;
        this.addServiceUseCase = addServiceUseCase;
        this.addMaterialUseCase = addMaterialUseCase;
        this.submitBudgetUseCase = submitBudgetUseCase;
        this.finishServiceOrderUseCase = finishServiceOrderUseCase;
        this.deliverServiceOrderUseCase = deliverServiceOrderUseCase;
        this.getServiceOrderUseCase = getServiceOrderUseCase;
        this.listServiceOrdersUseCase = listServiceOrdersUseCase;
        this.mapper = mapper;
    }

    @PostMapping
    @Operation(summary = "Abre uma OS a partir do CPF/CNPJ do cliente e da placa do veiculo")
    public ResponseEntity<ServiceOrderResponse> open(@Valid @RequestBody OpenServiceOrderRequest request) {
        ServiceOrder order = openServiceOrderUseCase.execute(new OpenServiceOrderCommand(
            request.customerDocument(), request.plate(), request.employeeId()));
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponse(order));
    }

    @GetMapping
    @Operation(summary = "Lista ordens de servico, opcionalmente filtrando por status")
    public List<ServiceOrderResponse> list(@RequestParam(required = false) String status) {
        return listServiceOrdersUseCase.execute(status)
            .stream()
            .map(mapper::toResponse)
            .toList();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Detalha uma ordem, com itens, orcamento e historico de status")
    public ServiceOrderResponse findById(@PathVariable Long id) {
        return mapper.toResponse(getServiceOrderUseCase.execute(id));
    }

    @PostMapping("/{id}/diagnosis")
    @Operation(summary = "Inicia o diagnostico (RECEIVED -> IN_DIAGNOSIS)")
    public ServiceOrderResponse startDiagnosis(@PathVariable Long id,
                                               @Valid @RequestBody EmployeeActionRequest request) {
        return mapper.toResponse(startDiagnosisUseCase.execute(id, request.employeeId()));
    }

    @PostMapping("/{id}/services")
    @Operation(summary = "Inclui um servico do catalogo e recalcula o orcamento")
    public ServiceOrderResponse addService(@PathVariable Long id,
                                           @Valid @RequestBody AddServiceRequest request) {
        return mapper.toResponse(addServiceUseCase.execute(id, request.serviceId(), request.employeeId()));
    }

    @PostMapping("/{id}/materials")
    @Operation(summary = "Inclui peca/insumo e recalcula o orcamento (sem baixa de estoque)")
    public ServiceOrderResponse addMaterial(@PathVariable Long id,
                                            @Valid @RequestBody AddMaterialRequest request) {
        return mapper.toResponse(
            addMaterialUseCase.execute(id, request.materialId(), request.quantity(), request.employeeId()));
    }

    @PostMapping("/{id}/budget")
    @Operation(summary = "Envia o orcamento ao cliente (-> AWAITING_APPROVAL)")
    public ServiceOrderResponse submitBudget(@PathVariable Long id,
                                             @Valid @RequestBody EmployeeActionRequest request) {
        return mapper.toResponse(submitBudgetUseCase.execute(id, request.employeeId()));
    }

    @PostMapping("/{id}/completion")
    @Operation(summary = "Finaliza a ordem (-> FINISHED)")
    public ServiceOrderResponse finish(@PathVariable Long id,
                                       @Valid @RequestBody FinishServiceOrderRequest request) {
        return mapper.toResponse(
            finishServiceOrderUseCase.execute(id, request.employeeId(), request.observation()));
    }

    @PostMapping("/{id}/delivery")
    @Operation(summary = "Registra a entrega do veiculo (-> DELIVERED)")
    public ServiceOrderResponse deliver(@PathVariable Long id,
                                        @Valid @RequestBody EmployeeActionRequest request) {
        return mapper.toResponse(deliverServiceOrderUseCase.execute(id, request.employeeId()));
    }
}

package com.postech.oficinamecanica.domain.serviceorder;

import com.postech.oficinamecanica.domain.shared.exceptions.InvalidParametersException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ServiceOrderTest {

    private static final Long CUSTOMER_ID = 10L;
    private static final Long VEHICLE_ID = 20L;
    private static final Long EMPLOYEE_ID = 30L;

    private ServiceOrder anOpenOrder() {
        return ServiceOrder.open(CUSTOMER_ID, VEHICLE_ID, EMPLOYEE_ID);
    }

    private ServiceOrder anOrderInDiagnosis() {
        ServiceOrder order = anOpenOrder();
        order.startDiagnosis(EMPLOYEE_ID);
        return order;
    }

    @Test
    void shouldOpenOrderAsReceivedWithoutBudget() {
        ServiceOrder order = anOpenOrder();

        assertThat(order.getStatus()).isEqualTo(ServiceOrderStatus.RECEIVED);
        assertThat(order.getPrice()).isNull();
        assertThat(order.getServices()).isEmpty();
        assertThat(order.getMaterials()).isEmpty();
        assertThat(order.getHistory()).hasSize(1);
        assertThat(order.getHistory().get(0).getStatus()).isEqualTo(ServiceOrderStatus.RECEIVED);
        assertThat(order.getHistory().get(0).getAuthorType()).isEqualTo(AuthorType.EMPLOYEE);
        assertThat(order.getHistory().get(0).getAuthorId()).isEqualTo(EMPLOYEE_ID);
    }

    @Test
    void shouldRequireCustomerAndVehicle() {
        assertThatThrownBy(() -> ServiceOrder.open(null, VEHICLE_ID, EMPLOYEE_ID))
            .isInstanceOf(InvalidParametersException.class);
        assertThatThrownBy(() -> ServiceOrder.open(CUSTOMER_ID, null, EMPLOYEE_ID))
            .isInstanceOf(InvalidParametersException.class);
    }

    @Test
    void shouldCalculateBudgetFromServicesAndMaterials() {
        ServiceOrder order = anOrderInDiagnosis();

        order.addService(1L, new BigDecimal("150.00"));
        order.addMaterial(2L, 3, new BigDecimal("20.00"));

        assertThat(order.getPrice()).isEqualByComparingTo(new BigDecimal("210.00"));
    }

    @Test
    void shouldAcceptRequestedServiceAlreadyAtOpening() {
        ServiceOrder order = anOpenOrder();

        order.addService(1L, new BigDecimal("80.00"));

        assertThat(order.getStatus()).isEqualTo(ServiceOrderStatus.RECEIVED);
        assertThat(order.getPrice()).isEqualByComparingTo(new BigDecimal("80.00"));
    }

    @Test
    void shouldRejectItemsAfterBudgetWasSentToCustomer() {
        ServiceOrder order = anOrderInDiagnosis();
        order.addService(1L, BigDecimal.TEN);
        order.submitForApproval(EMPLOYEE_ID);

        assertThatThrownBy(() -> order.addService(2L, BigDecimal.TEN))
            .isInstanceOf(ServiceOrderNotOpenForItemsException.class);
    }

    @Test
    void shouldRejectBudgetSubmissionWithoutItems() {
        ServiceOrder order = anOrderInDiagnosis();

        assertThatThrownBy(() -> order.submitForApproval(EMPLOYEE_ID))
            .isInstanceOf(EmptyServiceOrderException.class);
        assertThat(order.getStatus()).isEqualTo(ServiceOrderStatus.IN_DIAGNOSIS);
    }

    @Test
    void shouldWalkTheHappyPathUntilDelivered() {
        ServiceOrder order = anOrderInDiagnosis();
        order.addService(1L, new BigDecimal("100.00"));
        order.submitForApproval(EMPLOYEE_ID);
        order.approveBudget();
        order.finish(EMPLOYEE_ID, "Servicos concluidos");
        order.deliver(EMPLOYEE_ID);

        assertThat(order.getStatus()).isEqualTo(ServiceOrderStatus.DELIVERED);
        assertThat(order.getHistory()).extracting(ServiceOrderHistory::getStatus)
            .containsExactly(
                ServiceOrderStatus.RECEIVED,
                ServiceOrderStatus.IN_DIAGNOSIS,
                ServiceOrderStatus.AWAITING_APPROVAL,
                ServiceOrderStatus.IN_EXECUTION,
                ServiceOrderStatus.FINISHED,
                ServiceOrderStatus.DELIVERED);
    }

    @Test
    void shouldRejectInvalidTransition() {
        ServiceOrder order = anOpenOrder();

        assertThatThrownBy(() -> order.deliver(EMPLOYEE_ID))
            .isInstanceOf(InvalidServiceOrderTransitionException.class);
        assertThat(order.getStatus()).isEqualTo(ServiceOrderStatus.RECEIVED);
    }

    @Test
    void shouldRecordCustomerAsAuthorWhenBudgetIsRejected() {
        ServiceOrder order = anOrderInDiagnosis();
        order.addService(1L, new BigDecimal("100.00"));
        order.submitForApproval(EMPLOYEE_ID);

        order.rejectBudget("Vou fazer em outro lugar");

        ServiceOrderHistory last = order.getHistory().get(order.getHistory().size() - 1);
        assertThat(order.getStatus()).isEqualTo(ServiceOrderStatus.FINISHED);
        assertThat(last.getAuthorType()).isEqualTo(AuthorType.CUSTOMER);
        assertThat(last.getAuthorId()).isEqualTo(CUSTOMER_ID);
        assertThat(last.getObservation()).isEqualTo("Vou fazer em outro lugar");
    }

    @Test
    void shouldKeepBudgetOfEachTransitionInHistory() {
        ServiceOrder order = anOrderInDiagnosis();
        order.addService(1L, new BigDecimal("100.00"));
        order.submitForApproval(EMPLOYEE_ID);

        ServiceOrderHistory submission = order.getHistory().get(2);
        assertThat(order.getHistory().get(0).getPrice()).isNull();
        assertThat(submission.getPrice()).isEqualByComparingTo(new BigDecimal("100.00"));
    }

    @Test
    void shouldExposeOnlyMaterialsWaitingForStockDebit() {
        ServiceOrder order = anOrderInDiagnosis();
        order.addMaterial(2L, 1, new BigDecimal("50.00"));
        order.submitForApproval(EMPLOYEE_ID);
        order.approveBudget();
        order.markCycleApproved();

        order.addMaterial(3L, 2, new BigDecimal("30.00"));
        order.submitForApproval(EMPLOYEE_ID);
        order.approveBudget();

        List<ServiceOrderMaterial> pending = order.pendingStockDebit();

        assertThat(pending).hasSize(1);
        assertThat(pending.get(0).getMaterialId()).isEqualTo(3L);
        assertThat(order.getPrice()).isEqualByComparingTo(new BigDecimal("110.00"));
    }

    @Test
    void shouldNotLetCallersMutateAggregateCollections() {
        ServiceOrder order = anOrderInDiagnosis();
        order.addService(1L, BigDecimal.TEN);

        List<ServiceOrderService> services = order.getServices();

        assertThatThrownBy(() -> services.add(ServiceOrderService.of(9L, BigDecimal.ONE)))
            .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void shouldCancelOrderAndKeepReasonInHistory() {
        ServiceOrder order = anOrderInDiagnosis();
        order.addService(1L, new BigDecimal("100.00"));

        order.cancel(AuthorType.EMPLOYEE, EMPLOYEE_ID, "Cliente desistiu");

        ServiceOrderHistory last = order.getHistory().get(order.getHistory().size() - 1);
        assertThat(order.getStatus()).isEqualTo(ServiceOrderStatus.CANCELLED);
        assertThat(last.getObservation()).isEqualTo("Cliente desistiu");
        assertThat(last.getAuthorType()).isEqualTo(AuthorType.EMPLOYEE);
    }

    @Test
    void shouldCancelBySystemWhenOrderWasNeverExecuted() {
        ServiceOrder order = anOrderInDiagnosis();
        order.addMaterial(2L, 1, new BigDecimal("50.00"));
        order.submitForApproval(EMPLOYEE_ID);

        order.cancelBySystem("Peca 2 sem saldo");

        assertThat(order.getStatus()).isEqualTo(ServiceOrderStatus.CANCELLED);
        assertThat(order.getHistory().get(order.getHistory().size() - 1).getAuthorType())
            .isEqualTo(AuthorType.SYSTEM);
    }

    @Test
    void shouldDropOnlyTheAdditionalRepairWhenSystemCancelsAnExecutingOrder() {
        ServiceOrder order = anExecutingOrderWithAdditionalRepairPending();

        order.cancelBySystem("Orcamento sem resposta do cliente em 7 dias");

        assertThat(order.getStatus()).isEqualTo(ServiceOrderStatus.IN_EXECUTION);
        assertThat(order.getMaterials()).hasSize(1);
        assertThat(order.getMaterials().get(0).getMaterialId()).isEqualTo(2L);
        assertThat(order.getServices()).isEmpty();
        assertThat(order.getPrice()).isEqualByComparingTo(new BigDecimal("50.00"));
    }

    @Test
    void shouldDropOnlyTheAdditionalRepairWhenCustomerRejectsIt() {
        ServiceOrder order = anExecutingOrderWithAdditionalRepairPending();

        order.rejectBudget("Nao quero o servico extra");

        ServiceOrderHistory last = order.getHistory().get(order.getHistory().size() - 1);
        assertThat(order.getStatus()).isEqualTo(ServiceOrderStatus.IN_EXECUTION);
        assertThat(last.getAuthorType()).isEqualTo(AuthorType.CUSTOMER);
        assertThat(last.getObservation()).isEqualTo("Nao quero o servico extra");
        assertThat(order.getPrice()).isEqualByComparingTo(new BigDecimal("50.00"));
    }

    @Test
    void shouldStillFinishTheOrderWhenTheFirstBudgetIsRejected() {
        ServiceOrder order = anOrderInDiagnosis();
        order.addService(1L, new BigDecimal("100.00"));
        order.submitForApproval(EMPLOYEE_ID);

        order.rejectBudget("Preco alto");

        assertThat(order.getStatus()).isEqualTo(ServiceOrderStatus.FINISHED);
    }

    @Test
    void shouldExposeWhenTheBudgetWasSentToTheCustomer() {
        ServiceOrder order = anOrderInDiagnosis();
        assertThat(order.awaitingApprovalSince()).isNull();

        order.addService(1L, new BigDecimal("100.00"));
        order.submitForApproval(EMPLOYEE_ID);

        assertThat(order.awaitingApprovalSince()).isNotNull();
    }

    /** Ordem com um ciclo ja aprovado (peca 2) e um reparo adicional pendente (peca 3 + servico). */
    private ServiceOrder anExecutingOrderWithAdditionalRepairPending() {
        ServiceOrder order = anOrderInDiagnosis();
        order.addMaterial(2L, 1, new BigDecimal("50.00"));
        order.submitForApproval(EMPLOYEE_ID);
        order.approveBudget();
        order.markCycleApproved();

        order.addMaterial(3L, 2, new BigDecimal("30.00"));
        order.addService(9L, new BigDecimal("80.00"));
        order.submitForApproval(EMPLOYEE_ID);
        return order;
    }
}

package com.postech.oficinamecanica.infrastructure.notification;

import com.postech.oficinamecanica.application.material.MaterialRepository;
import com.postech.oficinamecanica.application.service.ServiceRepository;
import com.postech.oficinamecanica.application.vehicle.VehicleRepository;
import com.postech.oficinamecanica.domain.customer.Customer;
import com.postech.oficinamecanica.domain.customer.Document;
import com.postech.oficinamecanica.domain.material.Material;
import com.postech.oficinamecanica.domain.serviceorder.ServiceOrder;
import com.postech.oficinamecanica.domain.service.Service;
import com.postech.oficinamecanica.domain.shared.EntityStatus;
import com.postech.oficinamecanica.domain.vehicle.Plate;
import com.postech.oficinamecanica.domain.vehicle.Vehicle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SmtpServiceOrderNotifierAdapterTest {
    private static final String FROM = "oficina@oficina.com";

    @Mock private JavaMailSender mailSender;
    @Mock private ServiceRepository serviceRepository;
    @Mock private MaterialRepository materialRepository;
    @Mock private VehicleRepository vehicleRepository;

    private SmtpServiceOrderNotifierAdapter notifier;

    @BeforeEach
    void setUp() {
        notifier = new SmtpServiceOrderNotifierAdapter(mailSender, FROM,
            serviceRepository, materialRepository, vehicleRepository);
    }

    private Customer customer() {
        return new Customer(7L, new Document("529.982.247-25"), "Maria Oliveira", "(31) 98765-4321",
            "maria@email.com", EntityStatus.ACTIVE, Instant.now(), Instant.now());
    }

    private ServiceOrder order() {
        ServiceOrder order = ServiceOrder.open(7L, 3L, 1L);
        order.startDiagnosis(1L);
        order.addService(5L, new BigDecimal("120.00"));
        order.submitForApproval(1L);
        return order;
    }

    private SimpleMailMessage sent() {
        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());
        return captor.getValue();
    }

    @Test
    void shouldGreetCustomerByFirstNameAndSummarizeBudgetWhenSubmitted() {
        ServiceOrder order = order();

        notifier.notifyBudgetSubmitted(order, customer());

        SimpleMailMessage message = sent();
        assertThat(message.getFrom()).isEqualTo(FROM);
        assertThat(message.getTo()).containsExactly("maria@email.com");
        assertThat(message.getSubject()).contains("nº " + order.getId()).contains("orçamento");
        assertThat(message.getText())
            .contains("Olá, Maria!")
            .contains("Aguardando a sua aprovação")
            .contains("Total: R$ 120,00")
            .contains("Atenciosamente");
    }

    @Test
    void shouldShowVehicleAndItemNamesWhenLookupsSucceed() {
        Vehicle vehicle = mock(Vehicle.class);
        when(vehicle.getBrand()).thenReturn("Fiat");
        when(vehicle.getModel()).thenReturn("Uno");
        when(vehicle.getPlate()).thenReturn(new Plate("ABC1234"));
        when(vehicleRepository.findById(3L)).thenReturn(Optional.of(vehicle));
        Service service = mock(Service.class);
        when(service.getName()).thenReturn("Troca de óleo");
        when(serviceRepository.findById(5L)).thenReturn(Optional.of(service));

        notifier.notifyBudgetSubmitted(order(), customer());

        assertThat(sent().getText())
            .contains("Fiat Uno (placa ABC-1234)")
            .contains("Troca de óleo: R$ 120,00");
    }

    @Test
    void shouldFallBackToGenericLabelsWhenLookupsFail() {
        when(vehicleRepository.findById(3L)).thenThrow(new IllegalStateException("banco indisponivel"));
        when(serviceRepository.findById(5L)).thenReturn(Optional.empty());

        notifier.notifyBudgetSubmitted(order(), customer());

        assertThat(sent().getText()).contains("Serviço nº 5: R$ 120,00");
    }

    @Test
    void shouldIncludeReasonInBodyWhenBudgetIsRejected() {
        ServiceOrder order = order();

        notifier.notifyBudgetRejected(order, customer(), "Preco alto");

        SimpleMailMessage message = sent();
        assertThat(message.getSubject()).contains("recusado");
        assertThat(message.getText()).contains("Motivo informado: Preco alto")
            .contains("Encerrada sem execução")
            .doesNotContain("pronta para retirada");
    }

    @Test
    void shouldExplainThatServiceContinuesWhenOnlyAdditionalBudgetIsRejected() {
        ServiceOrder order = order();
        order.approveBudget();

        notifier.notifyBudgetRejected(order, customer(), "Nao preciso");

        assertThat(sent().getText())
            .contains("orçamento adicional")
            .contains("Em execução")
            .contains("continua em andamento");
    }

    @Test
    void shouldAnnounceReadyForPickupWhenFinished() {
        notifier.notifyFinished(order(), customer());

        SimpleMailMessage message = sent();
        assertThat(message.getSubject()).contains("pronto para retirada");
        assertThat(message.getText()).contains("retirado");
    }

    @Test
    void shouldKeepTwoArgumentConstructorWorkingWithoutRepositories() {
        SmtpServiceOrderNotifierAdapter simple = new SmtpServiceOrderNotifierAdapter(mailSender, FROM);

        simple.notifyBudgetSubmitted(order(), customer());

        assertThat(sent().getText()).contains("Serviço nº 5");
    }

    @Test
    void shouldNotPropagateExceptionWhenMailSenderFails() {
        doThrow(new MailSendException("smtp indisponivel")).when(mailSender).send(any(SimpleMailMessage.class));

        assertThatCode(() -> notifier.notifyFinished(order(), customer())).doesNotThrowAnyException();
    }
}

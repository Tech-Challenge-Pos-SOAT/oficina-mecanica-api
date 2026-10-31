package com.postech.oficinamecanica.infrastructure.notification;

import com.postech.oficinamecanica.application.material.MaterialRepository;
import com.postech.oficinamecanica.application.service.ServiceRepository;
import com.postech.oficinamecanica.application.serviceorder.ServiceOrderNotifier;
import com.postech.oficinamecanica.application.vehicle.VehicleRepository;
import com.postech.oficinamecanica.domain.customer.Customer;
import com.postech.oficinamecanica.domain.material.Material;
import com.postech.oficinamecanica.domain.service.Service;
import com.postech.oficinamecanica.domain.serviceorder.ServiceOrder;
import com.postech.oficinamecanica.domain.serviceorder.ServiceOrderMaterial;
import com.postech.oficinamecanica.domain.serviceorder.ServiceOrderService;
import com.postech.oficinamecanica.domain.serviceorder.ServiceOrderStatus;
import com.postech.oficinamecanica.domain.vehicle.Vehicle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Envia por e-mail (SMTP via Brevo) o aviso de mudanca de status da ordem, em
 * linguagem simples para o cliente: saudacao pelo nome, resumo da ordem
 * (veiculo, situacao, itens e valores) e o que acontece a seguir.
 *
 * O status ja foi persistido antes de chegar aqui - falha de envio so' e'
 * logada, nunca propaga e desfaz a transicao. A busca de nomes (servicos,
 * pecas, veiculo) tambem e' tolerante: se falhar, o e-mail sai com nomes
 * genericos em vez de deixar de ser enviado.
 */
@Component
public class SmtpServiceOrderNotifierAdapter implements ServiceOrderNotifier {
    private static final Logger log = LoggerFactory.getLogger(SmtpServiceOrderNotifierAdapter.class);
    private static final Locale PT_BR = Locale.forLanguageTag("pt-BR");

    private final JavaMailSender mailSender;
    private final String from;
    private final ServiceRepository serviceRepository;
    private final MaterialRepository materialRepository;
    private final VehicleRepository vehicleRepository;

    @Autowired
    public SmtpServiceOrderNotifierAdapter(JavaMailSender mailSender, @Value("${mail.from}") String from,
                                           ServiceRepository serviceRepository,
                                           MaterialRepository materialRepository,
                                           VehicleRepository vehicleRepository) {
        this.mailSender = mailSender;
        this.from = from;
        this.serviceRepository = serviceRepository;
        this.materialRepository = materialRepository;
        this.vehicleRepository = vehicleRepository;
    }

    /** Sem acesso aos cadastros: o e-mail usa nomes genericos para itens e veiculo. */
    public SmtpServiceOrderNotifierAdapter(JavaMailSender mailSender, String from) {
        this(mailSender, from, null, null, null);
    }

    @Override
    public void notifyBudgetSubmitted(ServiceOrder order, Customer customer) {
        send(order, customer, "seu orçamento está pronto",
            "Preparamos o orçamento do serviço no seu veículo e ele já está disponível para a sua decisão.",
            "Confira os valores acima. O reparo só começa depois da sua aprovação. Para aprovar ou "
                + "recusar, fale com a oficina informando o número da ordem de serviço.",
            null, null);
    }

    @Override
    public void notifyBudgetApproved(ServiceOrder order, Customer customer) {
        send(order, customer, "orçamento aprovado, reparo em execução",
            "Recebemos a sua aprovação. O reparo do seu veículo já entrou em execução.",
            "Nossa equipe está cuidando do serviço. Avisaremos por e-mail assim que ele for concluído.",
            null, null);
    }

    @Override
    public void notifyBudgetRejected(ServiceOrder order, Customer customer, String reason) {
        // Recusa de um reparo adicional: a ordem volta a "em execução" e o serviço original segue.
        boolean reparoAdicional = order.getStatus() == ServiceOrderStatus.IN_EXECUTION;
        String abertura = reparoAdicional
            ? "Registramos que você recusou o orçamento adicional. Esse reparo extra não será executado."
            : "Registramos que você recusou o orçamento. Nenhum serviço será executado no seu veículo.";
        String proximoPasso = reparoAdicional
            ? "O serviço que você já aprovou continua em andamento e avisaremos quando for concluído."
            : "Se mudar de ideia ou quiser um novo orçamento, é só falar com a oficina informando o número "
                + "da ordem de serviço. Você pode retirar o veículo normalmente.";
        String situacao = reparoAdicional ? null : "Encerrada sem execução (orçamento recusado)";
        send(order, customer, "orçamento recusado", abertura, proximoPasso, reason, situacao);
    }

    @Override
    public void notifyFinished(ServiceOrder order, Customer customer) {
        send(order, customer, "serviço concluído, pronto para retirada",
            "Boa notícia: o serviço no seu veículo foi concluído.",
            "Seu veículo já pode ser retirado na oficina. Leve um documento com foto.",
            null, null);
    }

    private void send(ServiceOrder order, Customer customer, String assunto, String abertura,
                      String proximoPasso, String motivo, String situacao) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(customer.getEmail());
        message.setSubject("Oficina Mecânica | OS nº " + order.getId() + ": " + assunto);
        message.setText(body(order, customer, abertura, proximoPasso, motivo, situacao));
        try {
            mailSender.send(message);
        } catch (MailException e) {
            log.error("Falha ao enviar e-mail de notificacao da ordem {} para {}", order.getId(),
                customer.getEmail(), e);
        }
    }

    private String body(ServiceOrder order, Customer customer, String abertura, String proximoPasso,
                        String motivo, String situacao) {
        StringBuilder texto = new StringBuilder()
            .append("Olá, ").append(firstName(customer)).append("!\n\n")
            .append(abertura).append("\n\n")
            .append("Resumo da sua ordem de serviço\n")
            .append("Ordem de serviço: nº ").append(order.getId()).append('\n');

        String veiculo = describeVehicle(order.getVehicleId());
        if (veiculo != null) {
            texto.append("Veículo: ").append(veiculo).append('\n');
        }
        texto.append("Situação: ").append(situacao != null ? situacao : readable(order)).append('\n');

        List<String> itens = items(order);
        if (!itens.isEmpty()) {
            texto.append("\nItens e valores\n");
            itens.forEach(item -> texto.append("- ").append(item).append('\n'));
        }
        if (order.getPrice() != null) {
            texto.append("Total: ").append(money(order.getPrice())).append('\n');
        }
        if (motivo != null && !motivo.isBlank()) {
            texto.append("\nMotivo informado: ").append(motivo).append('\n');
        }

        return texto
            .append("\nO que acontece agora\n")
            .append(proximoPasso).append("\n\n")
            .append("Atenciosamente,\n")
            .append("Equipe Oficina Mecânica\n\n")
            .append("(Mensagem automática, não é necessário responder.)")
            .toString();
    }

    private List<String> items(ServiceOrder order) {
        List<String> lines = new ArrayList<>();
        for (ServiceOrderService item : order.getServices()) {
            lines.add(serviceName(item.getServiceId()) + ": " + money(item.getPrice()));
        }
        for (ServiceOrderMaterial item : order.getMaterials()) {
            lines.add(materialName(item.getMaterialId()) + " (" + item.getQuantity() + " un.): "
                + money(item.total()));
        }
        return lines;
    }

    private String serviceName(Long serviceId) {
        String fallback = "Serviço nº " + serviceId;
        if (serviceRepository == null) {
            return fallback;
        }
        try {
            return serviceRepository.findById(serviceId).map(Service::getName).orElse(fallback);
        } catch (RuntimeException e) {
            log.warn("Nao foi possivel buscar o nome do servico {} para o e-mail", serviceId, e);
            return fallback;
        }
    }

    private String materialName(Long materialId) {
        String fallback = "Peça nº " + materialId;
        if (materialRepository == null) {
            return fallback;
        }
        try {
            return materialRepository.findById(materialId).map(Material::getName).orElse(fallback);
        } catch (RuntimeException e) {
            log.warn("Nao foi possivel buscar o nome da peca {} para o e-mail", materialId, e);
            return fallback;
        }
    }

    private String describeVehicle(Long vehicleId) {
        if (vehicleRepository == null || vehicleId == null) {
            return null;
        }
        try {
            return vehicleRepository.findById(vehicleId)
                .map(this::describe)
                .orElse(null);
        } catch (RuntimeException e) {
            log.warn("Nao foi possivel buscar o veiculo {} para o e-mail", vehicleId, e);
            return null;
        }
    }

    private String describe(Vehicle vehicle) {
        return vehicle.getBrand() + " " + vehicle.getModel() + " (placa " + vehicle.getPlate().value() + ")";
    }

    private static String firstName(Customer customer) {
        String name = customer.getName() == null ? "" : customer.getName().trim();
        if (name.isEmpty()) {
            return "cliente";
        }
        return name.split("\\s+")[0];
    }

    private static String money(BigDecimal value) {
        return String.format(PT_BR, "R$ %,.2f", value);
    }

    private static String readable(ServiceOrder order) {
        return switch (order.getStatus()) {
            case RECEIVED -> "Recebida";
            case IN_DIAGNOSIS -> "Em diagnóstico";
            case AWAITING_APPROVAL -> "Aguardando a sua aprovação";
            case IN_EXECUTION -> "Em execução";
            case FINISHED -> "Finalizada, pronta para retirada";
            case DELIVERED -> "Entregue";
            case CANCELLED -> "Cancelada";
        };
    }
}

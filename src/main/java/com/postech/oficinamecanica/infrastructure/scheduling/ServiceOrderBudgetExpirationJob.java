package com.postech.oficinamecanica.infrastructure.scheduling;

import com.postech.oficinamecanica.application.serviceorder.ExpireServiceOrderBudgetsUseCase;
import com.postech.oficinamecanica.domain.serviceorder.ServiceOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Varre uma vez por dia as ordens paradas aguardando aprovacao e aplica o
 * prazo de resposta. O horario e' configuravel por
 * "serviceorder.budget-expiration-cron".
 */
@Component
public class ServiceOrderBudgetExpirationJob {
    private static final Logger log = LoggerFactory.getLogger(ServiceOrderBudgetExpirationJob.class);

    private final ExpireServiceOrderBudgetsUseCase expireServiceOrderBudgetsUseCase;

    public ServiceOrderBudgetExpirationJob(ExpireServiceOrderBudgetsUseCase expireServiceOrderBudgetsUseCase) {
        this.expireServiceOrderBudgetsUseCase = expireServiceOrderBudgetsUseCase;
    }

    @Scheduled(cron = "${serviceorder.budget-expiration-cron:0 0 3 * * *}", zone = "America/Sao_Paulo")
    public void run() {
        List<ServiceOrder> afetadas = expireServiceOrderBudgetsUseCase.execute();
        if (!afetadas.isEmpty()) {
            log.info("Prazo de aprovacao aplicado em {} ordem(ns) de servico", afetadas.size());
        }
    }
}

package com.mercadolibre.bootcamp.projeto_integrador.job;

import com.mercadolibre.bootcamp.projeto_integrador.service.IPurchaseOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ScheduledJob {
    @Autowired
    private IPurchaseOrderService servicePurchase;

    @Scheduled(cron = "0 */15 * ? * *")
    public void dropAbandonedPurchase() {
        servicePurchase.dropAbandonedPurchase(60);
    }
}

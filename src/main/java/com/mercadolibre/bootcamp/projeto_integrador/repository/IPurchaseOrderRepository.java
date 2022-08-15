package com.mercadolibre.bootcamp.projeto_integrador.repository;

import com.mercadolibre.bootcamp.projeto_integrador.model.Buyer;
import com.mercadolibre.bootcamp.projeto_integrador.model.PurchaseOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface IPurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long> {
    PurchaseOrder findOnePurchaseOrderByBuyerAndOrderStatusIsLike(Buyer buyer, String orderStatus);
    PurchaseOrder findOneByPurchaseIdAndBuyer(long orderId, Buyer buyer);
    List<PurchaseOrder> findByOrderStatusAndUpdateDateTimeBefore(String status, LocalDateTime dateTime);
}

package com.mercadolibre.bootcamp.projeto_integrador.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.mercadolibre.bootcamp.projeto_integrador.enums.OrderStatus;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
public class PurchaseOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long purchaseId;
    private LocalDate date;
    private LocalDateTime updateDateTime;

    @Column(columnDefinition = OrderStatus.mysqlDefinition)
    @Enumerated(EnumType.STRING)
    private OrderStatus orderStatus;

    @ManyToOne
    @JoinColumn(name = "buyer_id", nullable = false)
    @JsonIgnore
    private Buyer buyer;

    @OneToMany(mappedBy = "purchaseOrder")
    private List<BatchPurchaseOrder> batchPurchaseOrders;

}

package com.mercadolibre.bootcamp.projeto_integrador.dto;

import com.mercadolibre.bootcamp.projeto_integrador.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseOrderResponseDto {
    private long purchaseOrderId;
    private OrderStatus orderStatus;
    private BigDecimal totalPrice;
    private List<BatchBuyerResponseDto> batches;
}

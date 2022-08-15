package com.mercadolibre.bootcamp.projeto_integrador.service;

import com.mercadolibre.bootcamp.projeto_integrador.dto.BatchBuyerResponseDto;
import com.mercadolibre.bootcamp.projeto_integrador.dto.BatchPurchaseOrderRequestDto;
import com.mercadolibre.bootcamp.projeto_integrador.dto.PurchaseOrderRequestDto;
import com.mercadolibre.bootcamp.projeto_integrador.dto.PurchaseOrderResponseDto;

import java.util.List;

public interface IPurchaseOrderService {
    PurchaseOrderResponseDto create (PurchaseOrderRequestDto request, long buyerId);
    PurchaseOrderResponseDto update (long purchaseOrderId, long buyerId);
    void dropProducts(long purchaseOrderId, BatchPurchaseOrderRequestDto batchDto, long buyerId);
    List<BatchBuyerResponseDto> getBatches(long buyerId, long purchaseOrderId);
    void dropAbandonedPurchase(long dropoutTimeInMinutes);
}

package com.mercadolibre.bootcamp.projeto_integrador.controller;

import com.mercadolibre.bootcamp.projeto_integrador.dto.BatchBuyerResponseDto;
import com.mercadolibre.bootcamp.projeto_integrador.dto.BatchPurchaseOrderRequestDto;
import com.mercadolibre.bootcamp.projeto_integrador.dto.PurchaseOrderRequestDto;
import com.mercadolibre.bootcamp.projeto_integrador.dto.PurchaseOrderResponseDto;
import com.mercadolibre.bootcamp.projeto_integrador.service.PurchaseOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@Validated
@RequestMapping("/api/v1")
public class PurchaseOrderController {

    @Autowired
    private PurchaseOrderService service;

    @PostMapping("/fresh-products/orders")
    public ResponseEntity<PurchaseOrderResponseDto> createPurchaseOrder(@RequestHeader("Buyer-Id") long buyerId,
                                                                        @RequestBody @Valid PurchaseOrderRequestDto purchaseOrder) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(purchaseOrder, buyerId));
    }

    @PutMapping("/fresh-products/orders")
    public ResponseEntity<PurchaseOrderResponseDto> updatePurchaseOrder(@RequestHeader("Buyer-Id") long buyerId,
                                                          @RequestParam long purchaseOrderId) {
        return ResponseEntity.ok(service.update(purchaseOrderId, buyerId));
    }

    @DeleteMapping("/fresh-products/orders")
    public ResponseEntity<Void> dropProductsPurchaseOrder(@RequestHeader("Buyer-Id") long buyerId,
                                                          @RequestParam long purchaseOrderId,
                                                          @RequestBody BatchPurchaseOrderRequestDto batchDto) {
        service.dropProducts(purchaseOrderId, batchDto, buyerId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/fresh-products/orders")
    public ResponseEntity<List<BatchBuyerResponseDto>> getProductsPurchaseOrder(@RequestHeader("Buyer-Id") long buyerId,
                                                                                @RequestParam long purchaseOrderId) {
        return ResponseEntity.ok(service.getBatches(buyerId, purchaseOrderId));
    }

    @PostMapping("/fresh-products/clean-orders")
    public ResponseEntity<Void> dropAbandonedPurchase() {
        service.dropAbandonedPurchase(120);
        return ResponseEntity.noContent().build();
    }
}

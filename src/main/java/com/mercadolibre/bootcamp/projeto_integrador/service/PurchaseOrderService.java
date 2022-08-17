package com.mercadolibre.bootcamp.projeto_integrador.service;

import com.mercadolibre.bootcamp.projeto_integrador.dto.BatchBuyerResponseDto;
import com.mercadolibre.bootcamp.projeto_integrador.dto.BatchPurchaseOrderRequestDto;
import com.mercadolibre.bootcamp.projeto_integrador.dto.PurchaseOrderRequestDto;
import com.mercadolibre.bootcamp.projeto_integrador.dto.PurchaseOrderResponseDto;
import com.mercadolibre.bootcamp.projeto_integrador.enums.OrderStatus;
import com.mercadolibre.bootcamp.projeto_integrador.exceptions.*;
import com.mercadolibre.bootcamp.projeto_integrador.model.Batch;
import com.mercadolibre.bootcamp.projeto_integrador.model.BatchPurchaseOrder;
import com.mercadolibre.bootcamp.projeto_integrador.model.Buyer;
import com.mercadolibre.bootcamp.projeto_integrador.model.PurchaseOrder;
import com.mercadolibre.bootcamp.projeto_integrador.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PurchaseOrderService implements IPurchaseOrderService {

    @Autowired
    IBuyerRepository buyerRepository;

    @Autowired
    IProductRepository productRepository;

    @Autowired
    IPurchaseOrderRepository purchaseOrderRepository;

    @Autowired
    IBatchRepository batchRepository;

    @Autowired
    IBatchPurchaseOrderRepository batchPurchaseOrderRepository;

    /**
     * Método que cria um carrinho (PurchaseOrder) novo ou insere/atualiza itens em um carrinho existente.
     *
     * @param request objeto PurchaseOrderRequestDto.
     * @return valor BigDecimal do valor total em carrinho.
     */
    @Transactional
    @Override
    public PurchaseOrderResponseDto create(PurchaseOrderRequestDto request, long buyerId) {
        Buyer buyer = findBuyer(buyerId);
        PurchaseOrder purchaseOrder = getPurchaseOrder(buyer, request.getOrderStatus());

        purchaseOrder = getPurchaseInStock(request.getBatch(), purchaseOrder);
        return new PurchaseOrderResponseDto(purchaseOrder.getPurchaseId(),
                purchaseOrder.getOrderStatus(), sumTotalPrice(purchaseOrder),
                mapListBatchPurchaseToListDto(purchaseOrder.getBatchPurchaseOrders()));
    }

    /**
     * Método que atualiza o carrinho (PurchaseOrder) para fechado.
     *
     * @param purchaseOrderId identificador do carrinho.
     * @return valor BigDecimal do valor total da compra.
     */
    @Transactional
    @Override
    public PurchaseOrderResponseDto update(long purchaseOrderId, long buyerId) {
        PurchaseOrder foundOrder = findPurchaseOrder(purchaseOrderId, buyerId);

        int initialQuantityOfBatchPurchases = foundOrder.getBatchPurchaseOrders().size();
        foundOrder = updateStockToPurchase(foundOrder);
        int currentQuantityOfBatchPurchases = foundOrder.getBatchPurchaseOrders().size();

        if (currentQuantityOfBatchPurchases == initialQuantityOfBatchPurchases) {
            foundOrder.setOrderStatus(OrderStatus.CLOSED);
        }
        foundOrder.setUpdateDateTime(LocalDateTime.now());
        purchaseOrderRepository.save(foundOrder);

        return new PurchaseOrderResponseDto(foundOrder.getPurchaseId(),
                foundOrder.getOrderStatus(), sumTotalPrice(foundOrder),
                mapListBatchPurchaseToListDto(foundOrder.getBatchPurchaseOrders()));
    }

    /**
     * Método que remove produto do carrinho.
     *
     * @param purchaseOrderId identificador do carrinho (PurchaseOrder).
     * @param batchDto        objeto BatchPurchaseOrderRequestDto com id do batch a ser retirado do carrinho.
     * @param buyerId         identificador do comprador.
     */
    @Transactional
    @Override
    public void dropProducts(long purchaseOrderId, BatchPurchaseOrderRequestDto batchDto, long buyerId) {
        PurchaseOrder purchaseOrder = findPurchaseOrder(purchaseOrderId, buyerId);
        BatchPurchaseOrder deleteBatchPurchase = findBatchPurchaseOrder(purchaseOrder, findBatchById(batchDto.getBatchNumber()));
        if (purchaseOrder.isReserved()) {
            returnToStock(deleteBatchPurchase);
        }
        batchPurchaseOrderRepository.delete(deleteBatchPurchase);
        purchaseOrder.setUpdateDateTime(LocalDateTime.now());
        purchaseOrderRepository.save(purchaseOrder);
    }

    /**
     * Método que busca a lista de compras do carrinho (PurchaseOrder) do cliente.
     *
     * @param buyerId         long.
     * @param purchaseOrderId long.
     * @return PurchaseOrder.
     * @throws NotFoundException if not exist opened Purchase
     */
    @Transactional
    @Override
    public List<BatchBuyerResponseDto> getBatches(long buyerId, long purchaseOrderId) {
        Buyer buyer = findBuyer(buyerId);
        PurchaseOrder purchaseOrder = purchaseOrderRepository.findOneByPurchaseIdAndBuyer(purchaseOrderId, buyer);
        if (purchaseOrder == null) {
            throw new NotFoundException("Purchase");
        }
        return mapListBatchPurchaseToListDto(purchaseOrder.getBatchPurchaseOrders());
    }

    /**
     * Método que devolve para o estoque os produtos dos carrinhos (PurchaseOrder) abandonados há mais de 1 horas.
     */
    @Transactional
    @Override
    public void dropAbandonedPurchase(long dropoutTimeInMinutes) {
        List<PurchaseOrder> abandonedPurchaseOrders = purchaseOrderRepository
                .findByOrderStatusAndUpdateDateTimeBefore(OrderStatus.OPENED, LocalDateTime.now().minusMinutes(dropoutTimeInMinutes));
        List<BatchPurchaseOrder> batchPurchaseOrders = abandonedPurchaseOrders.stream()
                .map(PurchaseOrder::getBatchPurchaseOrders)
                .collect(ArrayList::new, List::addAll, List::addAll);

        batchPurchaseOrders.stream().forEach(batch -> returnToStock(batch));
        abandonedPurchaseOrders.forEach(purchaseOrder -> purchaseOrder.setReserved(false));
    }

    private BatchPurchaseOrder returnToStock(BatchPurchaseOrder batchPurchaseOrder) {
        batchPurchaseOrder.getBatch().setCurrentQuantity(batchPurchaseOrder.getBatch().getCurrentQuantity() + batchPurchaseOrder.getQuantity());
        return batchPurchaseOrder;
    }

    private PurchaseOrder getPurchaseOrder(Buyer buyer, OrderStatus orderStatus) {
        PurchaseOrder purchaseOrder = purchaseOrderRepository.findOnePurchaseOrderByBuyerAndOrderStatusIsLike(buyer, OrderStatus.OPENED);
        if (purchaseOrder == null) {
            purchaseOrder = new PurchaseOrder();
            purchaseOrder.setBuyer(buyer);
            purchaseOrder.setDate(LocalDate.now());
            purchaseOrder.setReserved(false);
            purchaseOrderRepository.save(purchaseOrder);
        }
        purchaseOrder.setOrderStatus(orderStatus);
        purchaseOrder.setUpdateDateTime(LocalDateTime.now());
        return purchaseOrder;
    }

    private PurchaseOrder getPurchaseInStock(BatchPurchaseOrderRequestDto batchDto, PurchaseOrder purchase) {
        purchase = updateStockToPurchase(purchase);
        Optional<Batch> batchFound = batchRepository.findOneByBatchNumberAndCurrentQuantityGreaterThanEqualAndDueDateAfterOrderByDueDate(batchDto.getBatchNumber(),
                batchDto.getQuantity(), LocalDate.now().plusDays(21));

        if (batchFound.isEmpty()) throw new BatchOutOfStockException(batchDto.getBatchNumber());

        batchFound.get().setCurrentQuantity(batchFound.get().getCurrentQuantity() - batchDto.getQuantity());

        purchase = saveBatchPurchaseOrder(batchFound.get(), batchDto, purchase);
        purchase.setReserved(true);
        return purchase;
    }

    private PurchaseOrder updateStockToPurchase(PurchaseOrder purchase) {
        if (purchase.isReserved() || purchase.getBatchPurchaseOrders() == null || purchase.getBatchPurchaseOrders().isEmpty()) {
            return purchase;
        }

        List<BatchPurchaseOrder> batchPurchasesReserved = purchase.getBatchPurchaseOrders().stream()
                .filter(batchPurchase -> reserveBatch(batchPurchase.getBatch().getBatchNumber(), batchPurchase.getQuantity()))
                .collect(Collectors.toList());
        deleteOutOfStockBatchPurchase(purchase.getBatchPurchaseOrders(), batchPurchasesReserved);

        purchase.setBatchPurchaseOrders(batchPurchasesReserved);
        purchase.setReserved(true);
        return purchase;
    }

    private boolean reserveBatch(long batchNumber, int quantity) {
        Optional<Batch> batchFound = batchRepository.findOneByBatchNumberAndCurrentQuantityGreaterThanEqualAndDueDateAfterOrderByDueDate(batchNumber,
                quantity, LocalDate.now().plusDays(21));

        if (batchFound.isEmpty()) {
            return false;
        }
        batchFound.get().setCurrentQuantity(batchFound.get().getCurrentQuantity() - quantity);
        return true;
    }

    private void deleteOutOfStockBatchPurchase(List<BatchPurchaseOrder> allBatchPurchases, List<BatchPurchaseOrder> batchPurchasesReserved) {
        List<BatchPurchaseOrder> batchPurchases = allBatchPurchases.stream()
                .filter(bp -> !batchPurchasesReserved.contains(bp))
                .collect(Collectors.toList());
        batchPurchaseOrderRepository.deleteAll(batchPurchases);
    }

    private BigDecimal sumTotalPrice(PurchaseOrder purchase) {
        return purchase.getBatchPurchaseOrders().stream()
                .map(batchPurchaseOrder -> batchPurchaseOrder.getUnitPrice().multiply(new BigDecimal(batchPurchaseOrder.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private PurchaseOrder saveBatchPurchaseOrder(Batch batch, BatchPurchaseOrderRequestDto batchDto, PurchaseOrder purchase) {
        // Se já existir a tabela nxm entre um batch e uma purchase ela só é atualizada com a nova quantidade.
        BatchPurchaseOrder batchPurchaseOrder;
        try {
            batchPurchaseOrder = findBatchPurchaseOrder(purchase, batch);
        } catch (NotFoundException ex) {
            batchPurchaseOrder = new BatchPurchaseOrder();
            batchPurchaseOrder.setPurchaseOrder(purchase);
            batchPurchaseOrder.setBatch(batch);
            batchPurchaseOrder.setUnitPrice(batch.getProductPrice());
        }
        batchPurchaseOrder.setQuantity(batchPurchaseOrder.getQuantity() + batchDto.getQuantity());
        batchPurchaseOrderRepository.save(batchPurchaseOrder);

        if (purchase.getBatchPurchaseOrders() == null) {
            purchase.setBatchPurchaseOrders(new ArrayList<>());
        }
        if (!purchase.getBatchPurchaseOrders().contains(batchPurchaseOrder)) {
            purchase.getBatchPurchaseOrders().add(batchPurchaseOrder);
        }
        return purchase;
    }

    private PurchaseOrder findPurchaseOrder(long purchaseOrderId, long buyerId) {
        Optional<PurchaseOrder> foundOrder = purchaseOrderRepository.findById(purchaseOrderId);
        if (foundOrder.isEmpty()) throw new NotFoundException("Purchase order");
        if (foundOrder.get().getBuyer().getBuyerId() != buyerId)
            throw new UnauthorizedBuyerException(buyerId, purchaseOrderId);
        if (foundOrder.get().getOrderStatus().equals(OrderStatus.CLOSED))
            throw new PurchaseOrderAlreadyClosedException(foundOrder.get().getPurchaseId());
        return foundOrder.get();
    }

    private Buyer findBuyer(long buyerId) {
        Optional<Buyer> foundBuyer = buyerRepository.findById(buyerId);
        if (foundBuyer.isEmpty()) throw new NotFoundException("Buyer");
        return foundBuyer.get();
    }

    private Batch findBatchById(long batchNumber) {
        Optional<Batch> foundBatch = batchRepository.findById(batchNumber);
        if (foundBatch.isEmpty()) throw new NotFoundException("Batch");
        return foundBatch.get();
    }

    private BatchPurchaseOrder findBatchPurchaseOrder(PurchaseOrder purchase, Batch batch) {
        Optional<BatchPurchaseOrder> foundBatchPurchaseOrder = batchPurchaseOrderRepository.findOneByPurchaseOrderAndBatch(purchase, batch);
        if (foundBatchPurchaseOrder.isEmpty()) throw new NotFoundException("Batch PurchaseOrder");
        return foundBatchPurchaseOrder.get();
    }

    private List<BatchBuyerResponseDto> mapListBatchPurchaseToListDto(List<BatchPurchaseOrder> batches) {
        List<BatchBuyerResponseDto> batchBuyerResponse = batches.stream()
                .map(batch -> new BatchBuyerResponseDto(batch))
                .collect(Collectors.toList());
        return batchBuyerResponse;
    }
}

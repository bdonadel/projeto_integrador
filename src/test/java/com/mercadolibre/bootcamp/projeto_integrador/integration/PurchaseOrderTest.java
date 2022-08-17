package com.mercadolibre.bootcamp.projeto_integrador.integration;

import com.mercadolibre.bootcamp.projeto_integrador.dto.BatchPurchaseOrderRequestDto;
import com.mercadolibre.bootcamp.projeto_integrador.dto.BatchRequestDto;
import com.mercadolibre.bootcamp.projeto_integrador.dto.PurchaseOrderRequestDto;
import com.mercadolibre.bootcamp.projeto_integrador.dto.PurchaseOrderResponseDto;
import com.mercadolibre.bootcamp.projeto_integrador.enums.OrderStatus;
import com.mercadolibre.bootcamp.projeto_integrador.job.ScheduledJob;
import com.mercadolibre.bootcamp.projeto_integrador.model.*;
import com.mercadolibre.bootcamp.projeto_integrador.repository.IBatchPurchaseOrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class PurchaseOrderTest extends BaseControllerTest {
    @Autowired
    private ScheduledJob jobs;
    @Autowired
    private IBatchPurchaseOrderRepository batchPurchaseOrderRepository;

    private Manager manager;
    private Section freshSection;
    private Product freshProduct;
    private InboundOrder savedFreshInboundOrder;
    private Batch batchOfFreshSaved;
    private final int initialQuantity = 50;
    private Buyer buyer;

    @BeforeEach
    public void restart() {
        Warehouse warehouse = getSavedWarehouse();
        manager = getSavedManager();
        freshSection = getSavedFreshSection(warehouse, manager);
        freshProduct = getSavedFreshProduct();
        savedFreshInboundOrder = getSavedInboundOrder(freshSection);
        buyer = getSavedBuyer();
        BatchRequestDto batchOfFreshRequestDto = getValidBatchRequest(freshProduct);
        batchOfFreshRequestDto.setInitialQuantity(initialQuantity);
        batchOfFreshSaved = getSavedBatch(batchOfFreshRequestDto, savedFreshInboundOrder, freshProduct);
    }

    @Test
    void post_returnsPurchase_newPurchaseOrderOpened() throws Exception {
        // Arrange
        BatchPurchaseOrderRequestDto batch = new BatchPurchaseOrderRequestDto();
        batch.setBatchNumber(batchOfFreshSaved.getBatchNumber());
        batch.setQuantity(10);
        PurchaseOrderRequestDto order = newPurchaseOrderRequestDto(batch);

        // Act
        MvcResult response = mockMvc.perform(post("/api/v1/fresh-products/orders")
                .content(asJsonString(order))
                .header("Buyer-Id", buyer.getBuyerId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is2xxSuccessful())
                .andReturn();

        String json = response.getResponse().getContentAsString();
        PurchaseOrderResponseDto responseOrder = objectMapper.readValue(json, PurchaseOrderResponseDto.class);

        assertThat(responseOrder.getPurchaseOrderId()).isPositive();
        assertThat(responseOrder.getOrderStatus()).isEqualTo(OrderStatus.OPENED);
        assertThat(responseOrder.getTotalPrice())
                .isEqualTo(batchOfFreshSaved.getProductPrice().multiply(new BigDecimal("10")).setScale(2));
        assertThat(responseOrder.getBatches().size()).isEqualTo(1);
    }

    @Test
    void post_returnsPurchase_updatePurchaseOrderClosed() throws Exception {
        // Arrange
        BatchPurchaseOrderRequestDto batch = new BatchPurchaseOrderRequestDto();
        batch.setBatchNumber(batchOfFreshSaved.getBatchNumber());
        batch.setQuantity(10);
        PurchaseOrderRequestDto order = newPurchaseOrderRequestDto(batch);
        mockMvc.perform(post("/api/v1/fresh-products/orders")
                .content(asJsonString(order))
                .header("Buyer-Id", buyer.getBuyerId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is2xxSuccessful());

        order.setOrderStatus(OrderStatus.CLOSED);

        // Act
        MvcResult response = mockMvc.perform(post("/api/v1/fresh-products/orders")
                .content(asJsonString(order))
                .header("Buyer-Id", buyer.getBuyerId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is2xxSuccessful())
                .andReturn();

        String json = response.getResponse().getContentAsString();
        PurchaseOrderResponseDto responseOrder = objectMapper.readValue(json, PurchaseOrderResponseDto.class);

        assertThat(responseOrder.getPurchaseOrderId()).isPositive();
        assertThat(responseOrder.getOrderStatus()).isEqualTo(OrderStatus.CLOSED);
        assertThat(responseOrder.getTotalPrice())
                .isEqualTo(batchOfFreshSaved.getProductPrice().multiply(new BigDecimal("20")).setScale(2));
        assertThat(responseOrder.getBatches().size()).isEqualTo(1);
    }

    @Test
    void post_returnsPurchase_whenPurchaseOrderIsNotReserved() throws Exception {
        // Arrange
        BatchPurchaseOrderRequestDto batch = new BatchPurchaseOrderRequestDto();
        batch.setBatchNumber(batchOfFreshSaved.getBatchNumber());
        batch.setQuantity(10);
        PurchaseOrderRequestDto order = newPurchaseOrderRequestDto(batch);
        MvcResult resp = mockMvc.perform(post("/api/v1/fresh-products/orders")
                .content(asJsonString(order))
                .header("Buyer-Id", buyer.getBuyerId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is2xxSuccessful())
                .andReturn();

        String json = resp.getResponse().getContentAsString();
        long orderId = objectMapper.readValue(json, PurchaseOrderResponseDto.class).getPurchaseOrderId();
        PurchaseOrder purchaseOrder = purchaseOrderRepository.findById(orderId).get();
        purchaseOrder.setUpdateDateTime(LocalDateTime.now().minusHours(2));
        purchaseOrderRepository.save(purchaseOrder);
        jobs.dropAbandonedPurchase();
        batch.setQuantity(5);

        // Act
        MvcResult response = mockMvc.perform(post("/api/v1/fresh-products/orders")
                .content(asJsonString(order))
                .header("Buyer-Id", buyer.getBuyerId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is2xxSuccessful())
                .andReturn();

        // Assert
        json = response.getResponse().getContentAsString();
        PurchaseOrderResponseDto responseOrder = objectMapper.readValue(json, PurchaseOrderResponseDto.class);

        assertThat(responseOrder.getPurchaseOrderId()).isPositive();
        assertThat(responseOrder.getOrderStatus()).isEqualTo(OrderStatus.OPENED);
        assertThat(responseOrder.getTotalPrice())
                .isEqualTo(batchOfFreshSaved.getProductPrice().multiply(new BigDecimal("15")).setScale(2));
        assertThat(responseOrder.getBatches().size()).isEqualTo(1);

        PurchaseOrder orderAfterJob = purchaseOrderRepository.findById(orderId).get();
        assertThat(orderAfterJob.isReserved()).isTrue();
        BatchPurchaseOrder batchPurchase = batchPurchaseOrderRepository
                .findOneByPurchaseOrderAndBatch(orderAfterJob, batchOfFreshSaved).get();
        assertThat(batchPurchase.getQuantity()).isEqualTo(15);
        Batch batchAfterJob = batchRepository.findById(batchOfFreshSaved.getBatchNumber()).get();
        assertThat(batchAfterJob.getCurrentQuantity()).isEqualTo(initialQuantity - 15);
    }

    @Test
    void put_returnsPurchase_whenPurchaseOrderIsNotReserved() throws Exception {
        // Arrange
        BatchPurchaseOrderRequestDto batch = new BatchPurchaseOrderRequestDto();
        batch.setBatchNumber(batchOfFreshSaved.getBatchNumber());
        batch.setQuantity(10);
        PurchaseOrderRequestDto order = newPurchaseOrderRequestDto(batch);
        MvcResult resp = mockMvc.perform(post("/api/v1/fresh-products/orders")
                .content(asJsonString(order))
                .header("Buyer-Id", buyer.getBuyerId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is2xxSuccessful())
                .andReturn();

        String json = resp.getResponse().getContentAsString();
        long orderId = objectMapper.readValue(json, PurchaseOrderResponseDto.class).getPurchaseOrderId();
        PurchaseOrder purchaseOrder = purchaseOrderRepository.findById(orderId).get();
        purchaseOrder.setUpdateDateTime(LocalDateTime.now().minusHours(2));
        purchaseOrderRepository.save(purchaseOrder);
        jobs.dropAbandonedPurchase();

        // Act
        MvcResult response = mockMvc.perform(put("/api/v1/fresh-products/orders")
                .param("purchaseOrderId", "" + orderId)
                .header("Buyer-Id", buyer.getBuyerId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is2xxSuccessful())
                .andReturn();

        // Assert
        json = response.getResponse().getContentAsString();
        PurchaseOrderResponseDto responseOrder = objectMapper.readValue(json, PurchaseOrderResponseDto.class);

        assertThat(responseOrder.getOrderStatus()).isEqualTo(OrderStatus.CLOSED);
        assertThat(responseOrder.getTotalPrice())
                .isEqualTo(batchOfFreshSaved.getProductPrice().multiply(new BigDecimal("10")).setScale(2));
        assertThat(responseOrder.getBatches().size()).isEqualTo(1);

        PurchaseOrder orderAfterJob = purchaseOrderRepository.findById(orderId).get();
        assertThat(orderAfterJob.isReserved()).isTrue();
        BatchPurchaseOrder batchPurchase = batchPurchaseOrderRepository
                .findOneByPurchaseOrderAndBatch(orderAfterJob, batchOfFreshSaved).get();
        assertThat(batchPurchase.getQuantity()).isEqualTo(10);
        Batch batchAfterJob = batchRepository.findById(batchOfFreshSaved.getBatchNumber()).get();
        assertThat(batchAfterJob.getCurrentQuantity()).isEqualTo(initialQuantity - 10);
    }

    @Test
    void put_returnsPurchase_whenPurchaseOrderIsNotReservedAndThereIsNoStock() throws Exception {
        // Arrange
        BatchPurchaseOrderRequestDto batch = new BatchPurchaseOrderRequestDto();
        batch.setBatchNumber(batchOfFreshSaved.getBatchNumber());
        batch.setQuantity(10);
        PurchaseOrderRequestDto order = newPurchaseOrderRequestDto(batch);
        MvcResult resp = mockMvc.perform(post("/api/v1/fresh-products/orders")
                .content(asJsonString(order))
                .header("Buyer-Id", buyer.getBuyerId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is2xxSuccessful())
                .andReturn();

        String json = resp.getResponse().getContentAsString();
        long orderId = objectMapper.readValue(json, PurchaseOrderResponseDto.class).getPurchaseOrderId();
        PurchaseOrder purchaseOrder = purchaseOrderRepository.findById(orderId).get();
        purchaseOrder.setUpdateDateTime(LocalDateTime.now().minusHours(2));
        purchaseOrderRepository.save(purchaseOrder);
        jobs.dropAbandonedPurchase();
        BatchPurchaseOrder bp = batchPurchaseOrderRepository
                .findOneByPurchaseOrderAndBatch(purchaseOrder, batchOfFreshSaved).get();
        bp.setQuantity(initialQuantity + 5);
        batchPurchaseOrderRepository.save(bp);

        // Act
        MvcResult response = mockMvc.perform(put("/api/v1/fresh-products/orders")
                .param("purchaseOrderId", "" + orderId)
                .header("Buyer-Id", buyer.getBuyerId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is2xxSuccessful())
                .andReturn();

        // Assert
        json = response.getResponse().getContentAsString();
        PurchaseOrderResponseDto responseOrder = objectMapper.readValue(json, PurchaseOrderResponseDto.class);

        assertThat(responseOrder.getOrderStatus()).isEqualTo(OrderStatus.OPENED);
        assertThat(responseOrder.getTotalPrice()).isEqualTo(new BigDecimal("0"));
        assertThat(responseOrder.getBatches().size()).isEqualTo(0);

        PurchaseOrder orderAfterJob = purchaseOrderRepository.findById(orderId).get();
        assertThat(orderAfterJob.isReserved()).isTrue();
        Optional<BatchPurchaseOrder> batchPurchase = batchPurchaseOrderRepository
                .findOneByPurchaseOrderAndBatch(orderAfterJob, batchOfFreshSaved);
        assertThat(batchPurchase).isEqualTo(Optional.empty());
        Batch batchAfterJob = batchRepository.findById(batchOfFreshSaved.getBatchNumber()).get();
        assertThat(batchAfterJob.getCurrentQuantity()).isEqualTo(initialQuantity);
    }
}

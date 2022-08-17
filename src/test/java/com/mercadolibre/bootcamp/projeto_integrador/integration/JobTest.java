package com.mercadolibre.bootcamp.projeto_integrador.integration;

import com.mercadolibre.bootcamp.projeto_integrador.dto.BatchPurchaseOrderRequestDto;
import com.mercadolibre.bootcamp.projeto_integrador.dto.BatchRequestDto;
import com.mercadolibre.bootcamp.projeto_integrador.dto.PurchaseOrderRequestDto;
import com.mercadolibre.bootcamp.projeto_integrador.dto.PurchaseOrderResponseDto;
import com.mercadolibre.bootcamp.projeto_integrador.job.ScheduledJob;
import com.mercadolibre.bootcamp.projeto_integrador.model.*;
import com.mercadolibre.bootcamp.projeto_integrador.repository.IBatchPurchaseOrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class JobTest extends BaseControllerTest {
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
    public void dropAbandonedPurchase_doesNothing_whenThereIsNoAbandonedPurchase() throws Exception {
        // Arrange
        BatchPurchaseOrderRequestDto batch = new BatchPurchaseOrderRequestDto();
        batch.setBatchNumber(batchOfFreshSaved.getBatchNumber());
        batch.setQuantity(10);
        PurchaseOrderRequestDto order = newPurchaseOrderRequestDto(batch);

        MvcResult response = mockMvc.perform(post("/api/v1/fresh-products/orders")
                .content(asJsonString(order))
                .header("Buyer-Id", buyer.getBuyerId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is2xxSuccessful())
                .andReturn();

        String json = response.getResponse().getContentAsString();
        long orderId = objectMapper.readValue(json, PurchaseOrderResponseDto.class).getPurchaseOrderId();

        // Act
        jobs.dropAbandonedPurchase();

        // Assert
        PurchaseOrder orderAfterJob = purchaseOrderRepository.findById(orderId).get();
        assertThat(orderAfterJob).isNotNull();
        assertThat(orderAfterJob.isReserved()).isTrue();
        BatchPurchaseOrder batchPurchase = batchPurchaseOrderRepository
                .findOneByPurchaseOrderAndBatch(orderAfterJob, batchOfFreshSaved).get();
        assertThat(batchPurchase).isNotNull();
        Batch batchAfterJob = batchRepository.findById(batchOfFreshSaved.getBatchNumber()).get();
        assertThat(batchAfterJob.getCurrentQuantity()).isEqualTo(initialQuantity-10);
    }

    @Test
    public void dropAbandonedPurchase_dropPurchase_whenAbandonedPurchase() throws Exception {
        // Arrange
        BatchPurchaseOrderRequestDto batch = new BatchPurchaseOrderRequestDto();
        batch.setBatchNumber(batchOfFreshSaved.getBatchNumber());
        batch.setQuantity(10);
        PurchaseOrderRequestDto order = newPurchaseOrderRequestDto(batch);

        MvcResult response = mockMvc.perform(post("/api/v1/fresh-products/orders")
                .content(asJsonString(order))
                .header("Buyer-Id", buyer.getBuyerId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is2xxSuccessful())
                .andReturn();

        String json = response.getResponse().getContentAsString();
        long orderId = objectMapper.readValue(json, PurchaseOrderResponseDto.class).getPurchaseOrderId();
        PurchaseOrder purchaseOrder = purchaseOrderRepository.findById(orderId).get();
        purchaseOrder.setUpdateDateTime(LocalDateTime.now().minusHours(2));
        purchaseOrderRepository.save(purchaseOrder);

        // Act
        jobs.dropAbandonedPurchase();

        // Assert
        PurchaseOrder orderAfterJob = purchaseOrderRepository.findById(orderId).get();
        assertThat(orderAfterJob).isNotNull();
        assertThat(orderAfterJob.isReserved()).isFalse();
        BatchPurchaseOrder batchPurchase = batchPurchaseOrderRepository
                .findOneByPurchaseOrderAndBatch(orderAfterJob, batchOfFreshSaved).get();
        assertThat(batchPurchase).isNotNull();
        Batch batchAfterJob = batchRepository.findById(batchOfFreshSaved.getBatchNumber()).get();
        assertThat(batchAfterJob.getCurrentQuantity()).isEqualTo(initialQuantity);
    }
}
package com.mercadolibre.bootcamp.projeto_integrador.integration;
import com.fasterxml.jackson.core.JsonProcessingException;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class PurchaseOrderTest extends BaseControllerTest {
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
    void post_returnsPurchase_newPorchaseOrderOpened() throws Exception {
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
    void post_returnsPurchase_updatePorchaseOrderClosed() throws Exception {
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
}

package com.mercadolibre.bootcamp.projeto_integrador.integration;

import com.mercadolibre.bootcamp.projeto_integrador.dto.BatchRequestDto;
import com.mercadolibre.bootcamp.projeto_integrador.dto.InboundOrderRequestDto;
import com.mercadolibre.bootcamp.projeto_integrador.integration.listeners.ResetDatabase;
import com.mercadolibre.bootcamp.projeto_integrador.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SuppressWarnings("OptionalGetWithoutIsPresent")
@SpringBootTest
@AutoConfigureMockMvc
@ResetDatabase
public class CreateInboundOrderTest extends BaseControllerTest {
    private Manager manager;
    private Manager forbiddenManager;
    private Section sectionWithChilled;
    private Product frozenProduct;
    private Product product;
    private Section section;

    private InboundOrderRequestDto validInboundOrderRequest;
    private InboundOrderRequestDto invalidInboundOrderRequest;

    @BeforeEach
    void setup() {
        Warehouse warehouse = getSavedWarehouse();

        manager = getSavedManager();
        forbiddenManager = getSavedManager();

        section = getSavedFreshSection(warehouse, manager);
        sectionWithChilled = getSavedSection(warehouse, manager, Section.Category.CHILLED);

        product = getSavedFreshProduct();
        frozenProduct = getSavedProduct(Section.Category.FROZEN);

        validInboundOrderRequest = getValidInboundOrderRequestDto(section, getValidBatchRequest(product));
        invalidInboundOrderRequest = getValidInboundOrderRequestDto(section, getInvalidBatchRequestDto(product));
    }

    @Test
    void createInboundOrder_returnsCreated_whenIsGivenAValidInput() throws Exception {
        int quantityInboundOrder = inboundOrderRepository.findAll().size();
        int quantityBatch = batchRepository.findAll().size();

        mockMvc.perform(post("/api/v1/fresh-products/inboundorder")
                        .content(asJsonString(validInboundOrderRequest))
                        .header("Manager-Id", manager.getManagerId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());

        assertThat(inboundOrderRepository.findAll().size()).isEqualTo(quantityInboundOrder + 1);
        assertThat(batchRepository.findAll().size()).isEqualTo(quantityBatch + 1);
    }

    @Test
    void createInboundOrder_returnsError_whenIsGivenAnInvalidInput() throws Exception {
        int quantityInboundOrder = inboundOrderRepository.findAll().size();
        int quantityBatch = batchRepository.findAll().size();

        mockMvc.perform(post("/api/v1/fresh-products/inboundorder")
                        .content(asJsonString(invalidInboundOrderRequest))
                        .header("Manager-Id", manager.getManagerId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());

        assertThat(inboundOrderRepository.findAll().size()).isEqualTo(quantityInboundOrder);
        assertThat(batchRepository.findAll().size()).isEqualTo(quantityBatch);
    }

    @Test
    void createInboundOrder_ignoresBatchNumbers_whenIsGivenAValidInputWithBatchNumbersSet() throws Exception {
        // Arrange
        final float FIRST_BATCH_TEMPERATURE = 30;
        final float SECOND_BATCH_TEMPERATURE = 50;

        BatchRequestDto firstBatch = getValidBatchRequest(product);
        firstBatch.setCurrentTemperature(FIRST_BATCH_TEMPERATURE);

        BatchRequestDto secondBatchWithSameId = getValidBatchRequest(product);
        secondBatchWithSameId.setBatchNumber(1L);
        secondBatchWithSameId.setCurrentTemperature(SECOND_BATCH_TEMPERATURE);

        // Sanity check (The database should be empty before the test)
        assertThat(batchRepository.findAll()).isEmpty();

        // Act
        mockMvc.perform(post("/api/v1/fresh-products/inboundorder")
                .content(asJsonString(getValidInboundOrderRequestDto(section, firstBatch)))
                .header("Manager-Id", manager.getManagerId())
                .contentType(MediaType.APPLICATION_JSON));

        mockMvc.perform(post("/api/v1/fresh-products/inboundorder")
                .content(asJsonString(getValidInboundOrderRequestDto(section, secondBatchWithSameId)))
                .header("Manager-Id", manager.getManagerId())
                .contentType(MediaType.APPLICATION_JSON));

        Batch batch1 = batchRepository.findById(1L).orElse(null);
        Batch batch2 = batchRepository.findById(2L).orElse(null);

        // Assert
        assertThat(batch1).isNotNull();
        assertThat(batch2).isNotNull();
        assertThat(batch1.getCurrentTemperature()).isEqualTo(FIRST_BATCH_TEMPERATURE);
        assertThat(batch2.getCurrentTemperature()).isEqualTo(SECOND_BATCH_TEMPERATURE);
    }

    @Test
    void createInboundOrder_returnsNotFound_whenIsGivenProductThatDoesNotExist() throws Exception {
        BatchRequestDto batchWithNonExistentProduct = getBatchRequest(999);

        mockMvc.perform(post("/api/v1/fresh-products/inboundorder")
                        .content(asJsonString(getValidInboundOrderRequestDto(section, batchWithNonExistentProduct)))
                        .header("Manager-Id", manager.getManagerId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void createInboundOrder_returnsError_whenIsGivenAManagerThatDoesNotHavePermission() throws Exception {
        int quantityInboundOrder = inboundOrderRepository.findAll().size();
        int quantityBatch = batchRepository.findAll().size();

        mockMvc.perform(post("/api/v1/fresh-products/inboundorder")
                        .content(asJsonString(validInboundOrderRequest))
                        .header("Manager-Id", forbiddenManager.getManagerId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());

        assertThat(inboundOrderRepository.findAll().size()).isEqualTo(quantityInboundOrder);
        assertThat(batchRepository.findAll().size()).isEqualTo(quantityBatch);
    }

    @Test
    void createInboundOrder_returnsError_whenIsNotGivenManagerIdHeader() throws Exception {
        int quantityInboundOrder = inboundOrderRepository.findAll().size();
        int quantityBatch = batchRepository.findAll().size();

        mockMvc.perform(post("/api/v1/fresh-products/inboundorder")
                        .content(asJsonString(validInboundOrderRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Header Manager-Id is required"));

        assertThat(inboundOrderRepository.findAll().size()).isEqualTo(quantityInboundOrder);
        assertThat(batchRepository.findAll().size()).isEqualTo(quantityBatch);
    }

    @Test
    void createInboundOrder_returnsError_whenIsGivenIncompatibleProducts() throws Exception {
        int quantityInboundOrder = inboundOrderRepository.findAll().size();
        int quantityBatch = batchRepository.findAll().size();
        BatchRequestDto batchRequest = getValidBatchRequest(frozenProduct);
        InboundOrderRequestDto requestDto = getValidInboundOrderRequestDto(sectionWithChilled, batchRequest);

        mockMvc.perform(post("/api/v1/fresh-products/inboundorder")
                        .content(asJsonString(requestDto))
                        .header("Manager-Id", manager.getManagerId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.name").value("Incompatible category"));

        assertThat(inboundOrderRepository.findAll().size()).isEqualTo(quantityInboundOrder);
        assertThat(batchRepository.findAll().size()).isEqualTo(quantityBatch);
    }
}

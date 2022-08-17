package com.mercadolibre.bootcamp.projeto_integrador.integration;

import com.mercadolibre.bootcamp.projeto_integrador.dto.BatchRequestDto;
import com.mercadolibre.bootcamp.projeto_integrador.dto.InboundOrderRequestDto;
import com.mercadolibre.bootcamp.projeto_integrador.dto.InboundOrderResponseDto;
import com.mercadolibre.bootcamp.projeto_integrador.integration.listeners.ResetDatabase;
import com.mercadolibre.bootcamp.projeto_integrador.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SuppressWarnings("OptionalGetWithoutIsPresent")
@SpringBootTest
@AutoConfigureMockMvc
@ResetDatabase
public class UpdateInboundOrderTest extends BaseControllerTest {
    private Warehouse warehouse;
    private Manager manager;
    private Manager forbiddenManager;
    private Section freshSection;
    private Product freshProduct;

    private InboundOrderRequestDto validInboundOrderRequest;
    private InboundOrder savedFreshInboundOrder;
    private long orderNumber;
    private BatchRequestDto batchOfFreshRequestDto;
    private Batch batchOfFreshSaved;

    @BeforeEach
    void setup() {
        warehouse = getSavedWarehouse();

        manager = getSavedManager();
        forbiddenManager = getSavedManager();

        freshSection = getSavedFreshSection(warehouse, manager);
        freshProduct = getSavedFreshProduct();

        savedFreshInboundOrder = getSavedInboundOrder(freshSection);
        orderNumber = savedFreshInboundOrder.getOrderNumber();
        batchOfFreshRequestDto = getValidBatchRequest(freshProduct);
        batchOfFreshSaved = getSavedBatch(batchOfFreshRequestDto, savedFreshInboundOrder, freshProduct);
        validInboundOrderRequest = getValidInboundOrderRequestDto(freshSection, batchOfFreshRequestDto);
    }

    @Test
    void updateInboundOrder_returnsOk_whenBatchExists() throws Exception {
        // Update current batch temperature
        long batchNumber = batchOfFreshSaved.getBatchNumber();
        Batch oldBatch = batchRepository.findById(batchNumber).get();
        float newTemperature = oldBatch.getCurrentTemperature() + 1;
        batchOfFreshRequestDto.setBatchNumber(batchNumber);
        batchOfFreshRequestDto.setCurrentTemperature(newTemperature);
        int quantityInboundOrder = inboundOrderRepository.findAll().size();

        mockMvc.perform(put("/api/v1/fresh-products/inboundorder")
                        .param("orderNumber", "" + orderNumber)
                        .content(asJsonString(getValidInboundOrderRequestDto(freshSection, batchOfFreshRequestDto)))
                        .header("Manager-Id", manager.getManagerId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.batchStock[0].currentTemperature").value(newTemperature))
                .andExpect(jsonPath("$.batchStock[0].batchNumber").value(batchNumber));

        assertThat(inboundOrderRepository.findAll().size()).isEqualTo(quantityInboundOrder);
        Batch currentBatch = batchRepository.findById(batchNumber).get();
        assertThat(currentBatch.getCurrentTemperature()).isEqualTo(oldBatch.getCurrentTemperature() + 1);
    }

    @Test
    void updateInboundOrder_returnsOk_whenBatchNotExists() throws Exception {
        InboundOrderRequestDto requestDto = getValidInboundOrderRequestDto(freshSection, batchOfFreshRequestDto);
        int quantityInboundOrder = inboundOrderRepository.findAll().size();
        int quantityBatch = batchRepository.findAll().size();

        MvcResult response = mockMvc.perform(put("/api/v1/fresh-products/inboundorder")
                        .param("orderNumber", "" + orderNumber)
                        .content(asJsonString(validInboundOrderRequest))
                        .header("Manager-Id", manager.getManagerId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.batchStock[0].batchNumber").value(batchOfFreshSaved.getBatchNumber() + 1))
                .andExpect(jsonPath("$.batchStock[0].productPrice").value(batchOfFreshRequestDto.getProductPrice()))
                .andReturn();

        String json = response.getResponse().getContentAsString();
        InboundOrderResponseDto responseDto = objectMapper.readValue(json, InboundOrderResponseDto.class);

        Batch batch = batchRepository.findById(responseDto.getBatchStock().get(0).getBatchNumber()).get();
        assertThat(batch).isNotNull();
        assertThat(batch.getInboundOrder().getOrderNumber()).isEqualTo(orderNumber);
        assertThat(inboundOrderRepository.findAll().size()).isEqualTo(quantityInboundOrder);
        assertThat(batchRepository.findAll().size()).isEqualTo(quantityBatch + 1);
    }

    @Test
    void updateInboundOrder_returnsNotFound_whenInboundOrderIdNotExists() throws Exception {
        InboundOrderRequestDto requestDto = getValidInboundOrderRequestDto(freshSection, batchOfFreshRequestDto);
        int quantityInboundOrder = inboundOrderRepository.findAll().size();
        int quantityBatch = batchRepository.findAll().size();

        mockMvc.perform(put("/api/v1/fresh-products/inboundorder")
                        .param("orderNumber", "" + orderNumber + 1)
                        .content(asJsonString(requestDto))
                        .header("Manager-Id", manager.getManagerId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.name").value("Inbound Order not found."));

        assertThat(inboundOrderRepository.findAll().size()).isEqualTo(quantityInboundOrder);
        assertThat(batchRepository.findAll().size()).isEqualTo(quantityBatch);
    }

    @Test
    void updateInboundOrder_returnsBadRequest_whenSectionDoesNotHaveEnoughSpace() throws Exception {
        int availableSlots = freshSection.getAvailableSlots();
        List<BatchRequestDto> batches = new ArrayList<>();
        for (int i = 0; i < availableSlots+1; i++) {
            batches.add(batchOfFreshRequestDto);
        }
        InboundOrderRequestDto requestDto = new InboundOrderRequestDto();
        requestDto.setSectionCode(freshSection.getSectionCode());
        requestDto.setBatchStock(batches);
        int quantityBatch = batchRepository.findAll().size();

        mockMvc.perform(put("/api/v1/fresh-products/inboundorder")
                        .param("orderNumber", "" + orderNumber)
                        .content(asJsonString(requestDto))
                        .header("Manager-Id", manager.getManagerId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.name").value("Section"))
                .andExpect(jsonPath("$.message", containsString("enough space")));

        assertThat(batchRepository.findAll().size()).isEqualTo(quantityBatch);
        assertThat(sectionRepository.findById(freshSection.getSectionCode()).get().getAvailableSlots())
                .isEqualTo(availableSlots);
    }

    @Test
    void updateInboundOrder_returnsBadRequest_whenInvalidInitialQuantity() throws Exception {
        int batchInicialQuantity = batchOfFreshSaved.getInitialQuantity();
        long batchNumber = batchOfFreshSaved.getBatchNumber();
        // Change initial quantity to less than what have been sold
        batchOfFreshRequestDto.setBatchNumber(batchNumber);
        batchOfFreshRequestDto.setInitialQuantity(batchInicialQuantity
                - (batchOfFreshSaved.getCurrentQuantity() + 1));
        InboundOrderRequestDto requestDto = getValidInboundOrderRequestDto(freshSection, batchOfFreshRequestDto);

        mockMvc.perform(put("/api/v1/fresh-products/inboundorder")
                        .param("orderNumber", "" + orderNumber)
                        .content(asJsonString(getValidInboundOrderRequestDto(freshSection, batchOfFreshRequestDto)))
                        .header("Manager-Id", manager.getManagerId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.name").value("Campo(s) inválido(s)"))
                .andExpect(jsonPath("$.message", containsString("quantidade inicial deve ser")));

        assertThat(batchRepository.findById(batchNumber).get().getInitialQuantity())
                .isEqualTo(batchInicialQuantity);
    }

    @Test
    void updateInboundOrder_returnsForbidden_whenOriginalInboundOrderIsFromAnotherManager() throws Exception {
        InboundOrderRequestDto requestDto = getValidInboundOrderRequestDto(freshSection, batchOfFreshRequestDto);
        int quantityBatch = batchRepository.findAll().size();

        mockMvc.perform(put("/api/v1/fresh-products/inboundorder")
                        .param("orderNumber", "" + orderNumber)
                        .content(asJsonString(requestDto))
                        .header("Manager-Id", forbiddenManager.getManagerId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());

        assertThat(batchRepository.findAll().size()).isEqualTo(quantityBatch);
    }

    @Test
    void updateInboundOrder_returnsBadRequest_whenBatchIsFromAnotherManager() throws Exception {
        // Save batch by another manager
        Section freshSectionFromAnotherManager = getSavedFreshSection(warehouse, forbiddenManager);
        InboundOrder freshInboundFromAnotherManager = getSavedInboundOrder(freshSectionFromAnotherManager);
        Batch batchOfFreshFromAnotherManager = getSavedBatch(batchOfFreshRequestDto, freshInboundFromAnotherManager, freshProduct);
        long batchNumberFromAnotherManager = batchOfFreshFromAnotherManager.getBatchNumber();
        float currentTemperature = batchOfFreshFromAnotherManager.getCurrentTemperature();
        // Set batchNumber
        batchOfFreshRequestDto.setBatchNumber(batchNumberFromAnotherManager);
        batchOfFreshRequestDto.setCurrentTemperature(currentTemperature + 5f);
        InboundOrderRequestDto requestDto = getValidInboundOrderRequestDto(freshSection, batchOfFreshRequestDto);

        mockMvc.perform(put("/api/v1/fresh-products/inboundorder")
                .param("orderNumber", "" + orderNumber)
                .content(asJsonString(requestDto))
                .header("Manager-Id", manager.getManagerId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        Batch batch = batchRepository.findById(batchNumberFromAnotherManager).get();
        assertThat(batch.getInboundOrder().getOrderNumber())
                .isEqualTo(batchOfFreshFromAnotherManager.getInboundOrder().getOrderNumber());
        assertThat(batch.getCurrentTemperature()).isEqualTo(currentTemperature);
    }

    @Test
    void updateInboundOrder_returnsBadRequest_whenIsNotGivenManagerIdHeader() throws Exception {
        InboundOrderRequestDto requestDto = getValidInboundOrderRequestDto(freshSection, batchOfFreshRequestDto);
        int quantityBatch = batchRepository.findAll().size();

        mockMvc.perform(put("/api/v1/fresh-products/inboundorder")
                        .param("orderNumber", "" + orderNumber)
                        .content(asJsonString(requestDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Header Manager-Id is required"));

        assertThat(batchRepository.findAll().size()).isEqualTo(quantityBatch);
    }

    @Test
    void updateInboundOrder_returnsBadRequest_whenIsGivenIncompatibleProducts() throws Exception {
        Product frozenProduct = getSavedProduct(Section.Category.FROZEN);
        BatchRequestDto batchOfFrozenRequest = getValidBatchRequest(frozenProduct);
        InboundOrderRequestDto incompatibleRequestDto = getValidInboundOrderRequestDto(freshSection, batchOfFrozenRequest);
        int quantityBatch = batchRepository.findAll().size();

        mockMvc.perform(put("/api/v1/fresh-products/inboundorder")
                        .param("orderNumber", "" + orderNumber)
                        .content(asJsonString(incompatibleRequestDto))
                        .header("Manager-Id", manager.getManagerId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.name").value("Incompatible category"));

        assertThat(batchRepository.findAll().size()).isEqualTo(quantityBatch);
    }

    @Test
    void updateInboundOrder_returnsBadRequest_whenIsGivenBatchesOfDifferentOrders() throws Exception {
        InboundOrder order1 = getSavedInboundOrder(freshSection);
        InboundOrder order2 = getSavedInboundOrder(freshSection);

        Batch batch1 = getSavedBatch(freshProduct, order1);
        Batch batch2 = getSavedBatch(freshProduct, order2);

        BatchRequestDto request1 = modelMapper.map(batch1, BatchRequestDto.class);
        BatchRequestDto request2 = modelMapper.map(batch2, BatchRequestDto.class);

        mockMvc.perform(put("/api/v1/fresh-products/inboundorder")
                        .param("orderNumber", String.valueOf(1L))
                        .content(asJsonString(getValidInboundOrderRequestDto(freshSection, request1, request2)))
                        .header("Manager-Id", manager.getManagerId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateInboundOrder_returnsBadRequest_whenIsGivenProductThatDoesNotExists() throws Exception {
        BatchRequestDto batchWithNonExistentProduct = getBatchRequest(999);

        mockMvc.perform(put("/api/v1/fresh-products/inboundorder")
                        .param("orderNumber", String.valueOf(1L))
                        .content(asJsonString(getValidInboundOrderRequestDto(freshSection, batchWithNonExistentProduct)))
                        .header("Manager-Id", manager.getManagerId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("There is no product with the specified id"));
    }
}

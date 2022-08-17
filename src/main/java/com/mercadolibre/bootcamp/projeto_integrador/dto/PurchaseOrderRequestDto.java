package com.mercadolibre.bootcamp.projeto_integrador.dto;

import javax.validation.Valid;
import javax.validation.constraints.*;

import com.mercadolibre.bootcamp.projeto_integrador.enums.OrderStatus;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class PurchaseOrderRequestDto {
    @NotNull(message = "O status da compra não pode estar vazio")
    private OrderStatus orderStatus;

    @NotNull(message = "Objeto batch é obrigatório")
    private @Valid BatchPurchaseOrderRequestDto batch;
}

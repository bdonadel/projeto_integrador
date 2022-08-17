package com.mercadolibre.bootcamp.projeto_integrador.enums;

import lombok.Getter;

public enum OrderStatus {
    OPENED("Opened"),
    CLOSED("Closed");

    @Getter
    private String status;

    public static final String mysqlDefinition = "enum('OPENED','CLOSED')";

    OrderStatus(String status) {
        this.status = status;
    }
}
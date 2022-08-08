package com.mercadolibre.bootcamp.projeto_integrador.exceptions;

import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

public class InitialQuantityException extends CustomException {
    /**
     * Throw CustomException with HTTP Status 400.
     * @throws CustomException
     * @param newInitialQuantity
     * @param selledProductsQuantity
     */
    public InitialQuantityException(int newInitialQuantity, int selledProductsQuantity) {
        super("Invalid batch quantity", "Couldn't update batch initial quantity. New initial quantity: " + newInitialQuantity +
               ". Selled products: " + selledProductsQuantity , HttpStatus.BAD_REQUEST, LocalDateTime.now());
    }
}

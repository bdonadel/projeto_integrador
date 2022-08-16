package com.mercadolibre.bootcamp.projeto_integrador.util;

import com.mercadolibre.bootcamp.projeto_integrador.model.Buyer;

public class BuyerGenerator {
    public static Buyer newBuyer() {
        Buyer buyer = new Buyer();
        buyer.setUsername("Pedro");
        return buyer;
    }
}

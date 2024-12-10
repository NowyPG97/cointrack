package com.app.cointrack.currency.factory;

import com.app.cointrack.currency.dto.CurrencyResponseDTO;

import java.util.Map;

public class CurrencyDTOFactory {
    private CurrencyDTOFactory() {}

    public static CurrencyResponseDTO create(Map.Entry<String, Map<String, Double>> currency) {
        return CurrencyResponseDTO.builder()
                .source(currency.getKey())
                .rates(currency.getValue())
                .build();
    }
}

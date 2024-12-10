package com.app.cointrack.currency.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CurrencyResponseDTO {
    String source;
    Map<String, Double> rates;
}

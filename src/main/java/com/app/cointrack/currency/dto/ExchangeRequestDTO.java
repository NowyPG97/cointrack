package com.app.cointrack.currency.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import jakarta.validation.constraints.NotBlank;

import java.util.Set;

@Getter
@Builder
public class ExchangeRequestDTO {
    @NotBlank(message = "From value is mandatory.")
    String from;
    @NotNull
    @Size(min = 1,  message = "You must select at least one currency to exchange.")
    Set<String> to;
    @Positive(message = "Amount must be positive.")
    double amount;
}

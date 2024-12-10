package com.app.cointrack.currency.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ExchangeResultDTO {
    @JsonIgnore
    String to;
    double rate;
    double amount;
    double result;
    double fee;
}

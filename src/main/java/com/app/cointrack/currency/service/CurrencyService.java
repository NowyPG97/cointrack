package com.app.cointrack.currency.service;

import com.app.cointrack.currency.dto.CurrencyResponseDTO;
import com.app.cointrack.currency.dto.ExchangeResultDTO;

import java.util.List;
import java.util.Set;

public interface CurrencyService {
    CurrencyResponseDTO getCurrency(String currency, Set<String> filters);
    List<ExchangeResultDTO> exchangeCurrencies(String from, Set<String> to, double amount);
}

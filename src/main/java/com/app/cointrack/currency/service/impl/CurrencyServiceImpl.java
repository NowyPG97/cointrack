package com.app.cointrack.currency.service.impl;

import com.app.cointrack.currency.dto.CurrencyResponseDTO;
import com.app.cointrack.currency.dto.ExchangeResultDTO;
import com.app.cointrack.currency.factory.CurrencyDTOFactory;
import com.app.cointrack.currency.provider.CurrencyProvider;
import com.app.cointrack.currency.service.CurrencyService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

@Service
@AllArgsConstructor
public class CurrencyServiceImpl implements CurrencyService {
    private final CurrencyProvider dataProvider;

    @Override
    public CurrencyResponseDTO getCurrency(String currency, Set<String> filters) {
        Entry<String, Map<String, Double>> results = dataProvider.getCurrency(currency, filters);
        return CurrencyDTOFactory.create(results);
    }

    @Override
    public List<ExchangeResultDTO> exchangeCurrencies(String from, Set<String> to, double amount) {
        Entry<String, Map<String, Double>> currencies = dataProvider.getCurrency(from, to);
        BigDecimal feeRate = BigDecimal.valueOf(0.01);
        return currencies.getValue().entrySet()
                .parallelStream()
                .map(entry -> performExchange(BigDecimal.valueOf(amount), feeRate, entry))
                .toList();
    }

    private ExchangeResultDTO performExchange(BigDecimal amount, BigDecimal feeRate, Entry<String, Double> to) {
        BigDecimal fee = amount.multiply(feeRate);
        BigDecimal amountAfterFee = amount.subtract(fee);
        BigDecimal exchangeRate = BigDecimal.valueOf(to.getValue());
        BigDecimal finalAmount = amountAfterFee.multiply(exchangeRate);
        return ExchangeResultDTO.builder()
                .to(to.getKey())
                .rate(exchangeRate.doubleValue())
                .amount(amount.doubleValue())
                .result(finalAmount.doubleValue())
                .fee(fee.doubleValue())
                .build();
    }
}

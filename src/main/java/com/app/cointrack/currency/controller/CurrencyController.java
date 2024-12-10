package com.app.cointrack.currency.controller;

import com.app.cointrack.currency.dto.CurrencyResponseDTO;
import com.app.cointrack.currency.dto.ExchangeRequestDTO;
import com.app.cointrack.currency.dto.ExchangeResultDTO;
import com.app.cointrack.currency.service.CurrencyService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/currencies")
@AllArgsConstructor
public class CurrencyController {
    private final CurrencyService currencyService;

    @GetMapping("/{currency}")
    public CurrencyResponseDTO getCurrency(@PathVariable String currency, @RequestParam(name = "filter", required = false) Set<String> filters) {
        return currencyService.getCurrency(currency, filters);
    }

    @PostMapping("/exchange")
    public ResponseEntity<Map<String, Object>> exchangeCurrencies(@Valid @RequestBody ExchangeRequestDTO dto) {
        List<ExchangeResultDTO> results = currencyService.exchangeCurrencies(dto.getFrom(), dto.getTo(), dto.getAmount());
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("from", dto.getFrom());
        results.forEach(result -> response.put(result.getTo(), result));
        return ResponseEntity.ok(response);
    }
}

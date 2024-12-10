package com.app.cointrack.currency;

import com.app.cointrack.IntegrationTest;
import com.app.cointrack.common.exception.ExternalApiCommunicationException;
import com.app.cointrack.common.exception.RequestValidationException;
import com.app.cointrack.currency.dto.ExchangeRequestDTO;
import com.app.cointrack.currency.provider.CurrencyProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.AbstractMap;
import java.util.Map;
import java.util.Set;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

public class CurrencyIntTest extends IntegrationTest {

    @MockitoBean
    private CurrencyProvider providerMock;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void shouldReturnCurrenciesList() throws Exception {
        String testCurrency = "USD";
        Set<String> testFilters = Set.of("EUR", "GBP");
        Map<String, Double> rates = Map.of(
                "EUR", 0.85,
                "GBP", 0.75
        );
        Map.Entry<String, Map<String, Double>> mockResponse =
                new AbstractMap.SimpleEntry<>(testCurrency, rates);
        when(providerMock.getCurrency(testCurrency, testFilters)).thenReturn(mockResponse);

        mockMvc.perform(MockMvcRequestBuilders.get("/currencies/" + testCurrency + "?filter=EUR,GBP")).andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.source").value(testCurrency))
                .andExpect(jsonPath("$.rates.EUR").value(0.85))
                .andExpect(jsonPath("$.rates.GBP").value(0.75));;
    }

    @Test
    public void shouldHandleExternalApiCommunicationException() throws Exception {
        when(providerMock.getCurrency(Mockito.anyString(), Mockito.anySet()))
                .thenThrow(new ExternalApiCommunicationException("External API error"));

        mockMvc.perform(MockMvcRequestBuilders.get("/currencies/USD?filter=EUR,GBP"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.message").value("External API error"))
                .andExpect(jsonPath("$.errorStatus").value("INTERNAL_SERVER_ERROR"));
    }

    @Test
    public void shouldHandleRequestValidationException() throws Exception {
        when(providerMock.getCurrency(Mockito.anyString(), Mockito.anySet()))
                .thenThrow(new RequestValidationException("Invalid request parameters"));

        mockMvc.perform(MockMvcRequestBuilders.get("/currencies/INVALID?filter=EUR,GBP"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.message").value("Invalid request parameters"))
                .andExpect(jsonPath("$.errorStatus").value("BAD_REQUEST"));
    }

    @Test
    public void shouldExchangeCurrencies() throws Exception {
        String testCurrency = "USD";
        Set<String> testFilters = Set.of("EUR", "GBP");
        Map<String, Double> rates = Map.of(
                "EUR", 0.85,
                "GBP", 0.75
        );
        Map.Entry<String, Map<String, Double>> mockResponse =
                new AbstractMap.SimpleEntry<>(testCurrency, rates);
        when(providerMock.getCurrency(testCurrency, testFilters)).thenReturn(mockResponse);
        ExchangeRequestDTO dto = createRequestDTO(testCurrency, testFilters, 100);

        mockMvc.perform(post("/currencies/exchange")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(convertObjectToJsonBytes(dto)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.from").value(testCurrency))
                .andExpect(jsonPath("$.EUR.rate").value(0.85))
                .andExpect(jsonPath("$.EUR.amount").value(100.0))
                .andExpect(jsonPath("$.EUR.result").value(84.15))
                .andExpect(jsonPath("$.EUR.fee").value(1.0))
                .andExpect(jsonPath("$.GBP.rate").value(0.75))
                .andExpect(jsonPath("$.GBP.amount").value(100.0))
                .andExpect(jsonPath("$.GBP.result").value(74.25))
                .andExpect(jsonPath("$.GBP.fee").value(1.0));
    }

    @Test
    public void shouldHandleExternalApiCommunicationExceptionDuringExchange() throws Exception {
        String testCurrency = "USD";
        Set<String> testFilters = Set.of("EUR", "GBP");
        when(providerMock.getCurrency(Mockito.anyString(), Mockito.anySet()))
                .thenThrow(new ExternalApiCommunicationException("External API error"));
        ExchangeRequestDTO dto = createRequestDTO(testCurrency, testFilters, 100);

        mockMvc.perform(post("/currencies/exchange")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(convertObjectToJsonBytes(dto)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.message").value("External API error"))
                .andExpect(jsonPath("$.errorStatus").value("INTERNAL_SERVER_ERROR"));
    }

    @Test
    public void shouldHandleRequestValidationExceptionDuringExchange() throws Exception {
        String testCurrency = "USD";
        Set<String> testFilters = Set.of("EUR", "GBP");
        when(providerMock.getCurrency(Mockito.anyString(), Mockito.anySet()))
                .thenThrow(new RequestValidationException("Invalid request parameters"));
        ExchangeRequestDTO dto = createRequestDTO(testCurrency, testFilters, 100);

        mockMvc.perform(post("/currencies/exchange")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(convertObjectToJsonBytes(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.message").value("Invalid request parameters"))
                .andExpect(jsonPath("$.errorStatus").value("BAD_REQUEST"));
    }

    @Test
    public void shouldHandleValidationErrorsDuringExchange() throws Exception {
        String invalidFromCurrency = "";
        Set<String> testFilters = Set.of("EUR", "GBP");
        ExchangeRequestDTO dto = createRequestDTO(invalidFromCurrency, testFilters, 100);

        mockMvc.perform(post("/currencies/exchange")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(convertObjectToJsonBytes(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.message").value("Invalid request content."))
                .andExpect(jsonPath("$.errorStatus").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.fieldsValidationResults[0].field").value("from"))
                .andExpect(jsonPath("$.fieldsValidationResults[0].message").value("From value is mandatory."));

        ExchangeRequestDTO dtoInvalidTo = createRequestDTO("USD", null, 100);
        mockMvc.perform(post("/currencies/exchange")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(convertObjectToJsonBytes(dtoInvalidTo)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.message").value("Invalid request content."))
                .andExpect(jsonPath("$.errorStatus").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.fieldsValidationResults[0].field").value("to"))
                .andExpect(jsonPath("$.fieldsValidationResults[0].message").value("must not be null"));

        ExchangeRequestDTO dtoInvalidAmount = createRequestDTO("USD", Set.of("EUR", "GBP"), -100);
        mockMvc.perform(post("/currencies/exchange")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(convertObjectToJsonBytes(dtoInvalidAmount)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.message").value("Invalid request content."))
                .andExpect(jsonPath("$.errorStatus").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.fieldsValidationResults[0].field").value("amount"))
                .andExpect(jsonPath("$.fieldsValidationResults[0].message").value("Amount must be positive."));
    }

    private ExchangeRequestDTO createRequestDTO(String from, Set<String> to, double amount) {
        return ExchangeRequestDTO.builder()
                .from(from)
                .to(to)
                .amount(amount)
                .build();
    }
}

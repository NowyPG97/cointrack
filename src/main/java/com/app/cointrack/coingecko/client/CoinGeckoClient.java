package com.app.cointrack.coingecko.client;

import com.app.cointrack.coingecko.dto.CoinGeckoCurrencyDTO;
import com.app.cointrack.common.exception.ExternalApiCommunicationException;
import com.app.cointrack.common.exception.RequestValidationException;
import com.app.cointrack.currency.provider.CurrencyProvider;

import java.io.IOException;
import java.util.Map.Entry;
import java.util.*;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CoinGeckoClient implements CurrencyProvider {
    private static final Map<String, String> supportedCurrenciesIdToSymbol = new HashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final OkHttpClient client;

    @Value("${coingecko.api.key}")
    private String apiKey;
    @Value("${coingecko.api.baseurl}")
    private String baseurl;

    public CoinGeckoClient(OkHttpClient client) {
        this.client = client;
    }

    @PostConstruct
    public void initSupportedCurrencies() {
        if (supportedCurrenciesIdToSymbol.isEmpty()) {
            try {
                List<CoinGeckoCurrencyDTO> currencies = getSupportedCurrencies();
                currencies.forEach(currency -> supportedCurrenciesIdToSymbol.put(currency.getId(), currency.getSymbol()));
            } catch (IOException e) {
                log.error("Cannot initialize CoinGecko supported currencies.", e);
            }
        }
    }

    @Override
    public Entry<String, Map<String, Double>> getCurrency(String currency, Set<String> filters) {
        String currencyId = getCurrencyId(currency);
        Set<String> chosenFilters =
                filters == null || filters.isEmpty() ? Set.of() : filters;
        String url = buildGetCurrencyUrl(currencyId, chosenFilters);
        return fetchCurrencyData(url);
    }

    private Entry<String, Map<String, Double>> fetchCurrencyData(String url) {
        try (Response response = executeGetCall(url)) {
            if (!response.isSuccessful() || response.body() == null) {
                log.error("Unsuccessful request to CoinGecko API. Status: {}.", response.code());
                throw new ExternalApiCommunicationException("Problem during communication with CoinGecko API.");
            }
            return objectMapper.readValue(response.body().string(),
                    new TypeReference<Entry<String, Map<String, Double>>>() {});
        } catch (Exception e) {
            log.error("Problem during communication with CoinGecko API.", e);
            throw new ExternalApiCommunicationException("Problem during communication with CoinGecko API.");
        }
    }

    private String getCurrencyId(String currency) {
        initSupportedCurrencies();
        if(supportedCurrenciesIdToSymbol.containsKey(currency)) {
            return currency;
        }
        throw new RequestValidationException("Given Currency is not supported.");
    }

    private List<CoinGeckoCurrencyDTO> getSupportedCurrencies() throws IOException {
        try (Response response = executeGetCall(baseurl + "/coins/list")) {
            if (response.code() == HttpStatus.OK.value() && response.body() != null) {
                return objectMapper.readValue(response.body().string(), objectMapper.getTypeFactory().constructCollectionType(List.class, CoinGeckoCurrencyDTO.class));
            }
        }
        return List.of();
    }

    private Response executeGetCall(String url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .get()
                .addHeader("accept", "application/json")
                .addHeader("x-cg-demo-api-key", apiKey)
                .build();
        return client.newCall(request).execute();
    }

    private String buildGetCurrencyUrl(String currencyId, Set<String> filters) {
        StringBuilder url = new StringBuilder(baseurl)
                .append("/simple/price?ids=")
                .append(currencyId)
                .append("&vs_currencies=");
        Iterator<String> iterator = filters.iterator();
        while (iterator.hasNext()) {
            String filter = iterator.next();
            url.append(filter);
            if(iterator.hasNext()) {
                url.append(",");
            }
        }
        return url.toString();
    }
}

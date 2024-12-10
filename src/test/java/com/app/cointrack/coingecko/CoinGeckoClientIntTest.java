package com.app.cointrack.coingecko;

import com.app.cointrack.IntegrationTest;
import com.app.cointrack.coingecko.client.CoinGeckoClient;
import com.app.cointrack.common.exception.ExternalApiCommunicationException;
import com.app.cointrack.common.exception.RequestValidationException;
import okhttp3.mockwebserver.*;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class CoinGeckoClientIntTest extends IntegrationTest {
    private static MockWebServer mockWebServer;

    @Autowired
    private CoinGeckoClient coinGeckoClient;

    static {
        try {
            mockWebServer = new MockWebServer();
            mockWebServer.start();
            setUpMockServer("");
        } catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    @DynamicPropertySource
    static void dynamicProperties(DynamicPropertyRegistry registry) {
        registry.add("coingecko.api.key", () -> "test-api-key");
        registry.add("coingecko.api.baseurl", () -> mockWebServer.url("").toString());
    }

    @AfterAll
    static void tearDown() throws Exception {
        mockWebServer.shutdown();
    }

    @Test
    public void shouldReturnSupportedCurrency() {
        setUpMockServer("?ids=bitcoin");

        Map.Entry<String, Map<String, Double>> result = coinGeckoClient.getCurrency("bitcoin", Set.of());

        assertThat(result.getKey()).isEqualTo("bitcoin");
        assertThat(result.getValue().size()).isEqualTo(1);
        assertThat(result.getValue()).containsEntry("usd", 40000.0);
    }

    @Test
    public void shouldThrowRequestValidationExceptionForUnsupportedCurrency() {
        setUpMockServer("");

        Assertions.assertThrows(RequestValidationException.class, () -> {
            coinGeckoClient.getCurrency("unsupportedCurrency", Set.of("usd"));
        });
    }

    @Test
    public void shouldHandleApiErrorGracefully() {
        setUpMockServerWithErrorResponse();

        Assertions.assertThrows(ExternalApiCommunicationException.class, () -> {
            coinGeckoClient.getCurrency("bitcoin", Set.of("usd"));
        });
    }

    @Test
    public void shouldReturnFilteredCurrencyData() {
        setUpMockServerWithFilteredResponse();

        Set<String> filters = Set.of("usd", "eur");
        Map.Entry<String, Map<String, Double>> result = coinGeckoClient.getCurrency("bitcoin", filters);

        assertThat(result.getKey()).isEqualTo("bitcoin");
        assertThat(result.getValue().size()).isEqualTo(2);
        assertThat(result.getValue()).containsEntry("usd", 40000.0);
        assertThat(result.getValue()).containsEntry("eur", 35000.0);
    }

    private static void setUpMockServer(String priceRequestParameters) {
        if(mockWebServer != null) {
            mockWebServer.setDispatcher(new Dispatcher() {
                @Override
                public MockResponse dispatch(RecordedRequest request) {
                    if (request.getPath().equals("//coins/list")) {
                        return new MockResponse()
                                .setResponseCode(200)
                                .setBody("[{\"id\":\"bitcoin\",\"symbol\":\"btc\",\"name\":\"Bitcoin\"}]")
                                .addHeader("Content-Type", "application/json");
                    } else if (request.getPath().startsWith("//simple/price" + priceRequestParameters)) {
                        return new MockResponse()
                                .setResponseCode(200)
                                .setBody("{\"bitcoin\":{\"usd\":40000.0}}")
                                .addHeader("Content-Type", "application/json");
                    }
                    return new MockResponse().setResponseCode(404);
                }
            });
        }
    }

    private static void setUpMockServerWithErrorResponse() {
        if (mockWebServer != null) {
            mockWebServer.setDispatcher(new Dispatcher() {
                @Override
                public MockResponse dispatch(RecordedRequest request) {
                    return new MockResponse()
                            .setResponseCode(500)
                            .addHeader("Content-Type", "application/json")
                            .setBody("{\"error\":\"Internal Server Error\"}");
                }
            });
        }
    }

    private static void setUpMockServerWithFilteredResponse() {
        if (mockWebServer != null) {
            mockWebServer.setDispatcher(new Dispatcher() {
                @Override
                public MockResponse dispatch(RecordedRequest request) {
                    if (request.getPath().contains("/simple/price")) {
                        return new MockResponse()
                                .setResponseCode(200)
                                .setBody("{\"bitcoin\":{\"usd\":40000.0, \"eur\":35000.0}}")
                                .addHeader("Content-Type", "application/json");
                    }
                    return new MockResponse().setResponseCode(404);
                }
            });
        }
    }
}

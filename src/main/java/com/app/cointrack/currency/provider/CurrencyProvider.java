package com.app.cointrack.currency.provider;

import java.util.Map.Entry;
import java.util.Map;
import java.util.Set;

public interface CurrencyProvider {
    Entry<String, Map<String, Double>> getCurrency(String currency, Set<String> filters);
}

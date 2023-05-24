package by.kaminsky.repository;

import by.kaminsky.exchangeRate.ExchangeRate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Repository
public class ExchangeRatesStorageImpl implements ExchangeRatesStorage {

    private final Map<String, ExchangeRate> exchangeRatesBYN;

    public ExchangeRatesStorageImpl() {
        exchangeRatesBYN = new HashMap<>();
    }

    public ExchangeRate getRate(String abb) {
        return exchangeRatesBYN.get(abb);
    }

    public void setRate(String abb, ExchangeRate ex) {
        exchangeRatesBYN.put(abb, ex);
    }

    public boolean checkExchangeRate(String abb) {
        if (!exchangeRatesBYN.containsKey(abb) || exchangeRatesBYN.get(abb) == null ||
                exchangeRatesBYN.get(abb).getOfficialRate() == null) {
            log.warn("Exchange rate {} is not found", abb);
            return false;
        } else {
            return true;
        }
    }

}

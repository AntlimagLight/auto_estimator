package by.kaminsky.repository;

import by.kaminsky.exchangeRate.ExchangeRate;

public interface ExchangeRatesStorage {

    ExchangeRate getRate(String abb);

    void setRate(String abb, ExchangeRate ex);

    boolean checkExchangeRate(String abb);
}

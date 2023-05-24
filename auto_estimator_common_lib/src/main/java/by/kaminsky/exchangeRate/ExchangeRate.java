package by.kaminsky.exchangeRate;

import java.time.LocalDate;

public interface ExchangeRate {

    LocalDate getDate();

    String getAbbreviation();

    Double getOfficialRate();

}

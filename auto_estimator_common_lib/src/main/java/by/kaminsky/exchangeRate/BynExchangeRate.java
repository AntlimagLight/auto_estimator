package by.kaminsky.exchangeRate;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.*;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@ToString
@Builder
public class BynExchangeRate implements ExchangeRate {
    private LocalDate date;
    private String abbreviation;
    private Double officialRate;

    @SuppressWarnings("rawtypes")
    public static BynExchangeRate requestRateUSD() {
        try {
            URL url = new URL("https://api.nbrb.by/exrates/rates/431");
            ObjectMapper objectMapper = new ObjectMapper();
            Map objMap = objectMapper.readValue(url, Map.class);
            Map<String, String> stringMap = new HashMap<>();
            for (var e : objMap.entrySet()) {
                val stringVal = e.toString();
                stringMap.put(stringVal.substring(0, stringVal.indexOf('=')),
                        stringVal.substring(stringVal.indexOf('=') + 1));
            }
            return BynExchangeRate.builder()
                    .abbreviation(stringMap.get("Cur_Abbreviation"))
                    .officialRate(Double.parseDouble(stringMap.get("Cur_OfficialRate")))
                    .date(LocalDateTime.parse(stringMap.get("Date")).toLocalDate())
                    .build();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

}

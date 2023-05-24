package by.kaminsky.dto;

import by.kaminsky.exchangeRate.ExchangeRate;
import lombok.*;

import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@ToString
@Builder
public class EstimateDataDto {

    private String projectName;
    private ExchangeRate exchangeRate;
    private Map<WorkDto, Integer> works;
    private Map<MaterialDto, Integer> materials;

}

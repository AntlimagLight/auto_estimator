package by.kaminsky.dto;

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
    private Map<WorkDto, Integer> works;
    private Map<MaterialDto, Integer> materials;

}

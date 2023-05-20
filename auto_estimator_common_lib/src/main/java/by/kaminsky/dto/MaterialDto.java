package by.kaminsky.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@ToString
@Builder
public class MaterialDto {

    private String name;
    private String specific;
    private String packaging;
    private BigDecimal cost;
    private String source;


}



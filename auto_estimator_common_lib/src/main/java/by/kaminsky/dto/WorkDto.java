package by.kaminsky.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@ToString
@Builder
@EqualsAndHashCode
public class WorkDto {

    private String name;
    private String packaging;
    private BigDecimal cost;

}



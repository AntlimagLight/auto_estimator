package by.kaminsky.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@ToString
public class MaterialDto {

    private String name;
    private String specific;
    private String packaging;
    private BigDecimal cost;

}



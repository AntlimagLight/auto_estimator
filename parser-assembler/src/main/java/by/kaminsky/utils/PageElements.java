package by.kaminsky.utils;

import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class PageElements {

    private List<Double> priceElements;
    private List<String> texts;

}

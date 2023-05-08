package by.kaminsky.utils;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class ParseOrder {

    private String materialName;
    private String materialAdditionalSpecific;
    private String materialPackaging;
    private String url;

}
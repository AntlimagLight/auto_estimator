package by.kaminsky.helper_objects;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class ParseOrder {

    private String materialName;
    private String materialAdditionalSpecific;
    private Double costModifier;
    private String materialPackaging;
    private String url;

}

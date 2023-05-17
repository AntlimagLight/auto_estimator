package by.kaminsky.util;

import by.kaminsky.dto.MaterialDto;
import by.kaminsky.enums.SourceCompanies;
import by.kaminsky.helper_objects.ParseOrder;

import java.math.BigDecimal;
import java.util.List;

public class MaterialUtils {

    // Converts the order for parsing into materials, without the parsing procedure. All data is taken only from the order.
    public static MaterialDto createCustomizedMaterial(ParseOrder parseOrder, SourceCompanies company) {
        return MaterialDto.builder()
                .name(parseOrder.getMaterialName().toLowerCase())
                .specific(parseOrder.getMaterialAdditionalSpecific())
                .packaging(parseOrder.getMaterialPackaging())
                .cost(BigDecimal.valueOf(parseOrder.getCostModifier()))
                .source(company.toString())
                .build();
    }

    public static List<MaterialDto> convertOrdersToMaterials(List<ParseOrder> orders, SourceCompanies company) {
        return orders.stream().map(parseOrder -> createCustomizedMaterial(parseOrder, company)).toList();
    }

}

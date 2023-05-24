package by.kaminsky.service.parse;

import by.kaminsky.dto.MaterialDto;
import by.kaminsky.enums.SourceCompanies;
import by.kaminsky.repository.ExchangeRatesStorage;
import by.kaminsky.service.ParseOrderService;
import by.kaminsky.util.MaterialUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomMaterialsService implements ParseService {

    private final ParseOrderService parseOrderService;
    private final ExchangeRatesStorage exchangeRatesStorage;

    @Override
    public List<MaterialDto> startParse() {
        log.info("Start adding custom materials");
        var orders = parseOrderService.prepareParseOrdersAndCheckForContent("kaminsky_own_matterials.txt");
        List<MaterialDto> materials =
                new LinkedList<>(MaterialUtils.convertOrdersToMaterials(orders, SourceCompanies.KAMINSKY));
//        The price of additional materials is provided in USA dollars
//        in the general list of materials from the parser, the price is indicated in Belarusian rubles.
        materials.addAll(new LinkedList<>(MaterialUtils.convertOrdersToMaterials(orders, SourceCompanies.KAMINSKY).stream()
                .peek(materialDto -> materialDto.setCost(BigDecimal.valueOf(materialDto.getCost().doubleValue() *
                        exchangeRatesStorage.getRate("USD").getOfficialRate())))
                .toList()));
        if (materials.isEmpty()) log.warn(this.getClass().getName() + " : No orders for add");
        return materials;
    }


}

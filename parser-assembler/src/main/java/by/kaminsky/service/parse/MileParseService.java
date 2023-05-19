package by.kaminsky.service.parse;

import by.kaminsky.dto.MaterialDto;
import by.kaminsky.enums.SourceCompanies;
import by.kaminsky.helper_objects.ParseOrder;
import by.kaminsky.service.ParseOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MileParseService implements ParseService {

    private final ParseOrderService parseOrderService;

    @Override
    public List<MaterialDto> startParse() {
        log.info("Start parsing Mile");
        var orders = parseOrderService.prepareParseOrdersAndCheckForContent("mile.txt");
        List<MaterialDto> materials = new LinkedList<>();
        for (var order : orders) {
            val material = parseMaterialFromMile(order);
            if (material != null) materials.add(material);
        }
        if (materials.isEmpty()) {
            log.warn(this.getClass().getName() + " : No orders for parse");
        }
        return materials;
    }

    private MaterialDto parseMaterialFromMile(ParseOrder parseOrder) {
        try {
            val doc = Jsoup.connect(parseOrder.getUrl()).get();
            val priceText = doc.select("span[itemprop=price]").get(0).text();
            val price = BigDecimal.valueOf(Double.parseDouble(priceText.replace(',', '.')) +
                            parseOrder.getCostModifier());
            val specific = doc.select("h1[itemprop=name]").get(0).text();
            return MaterialDto.builder()
                    .name(parseOrder.getMaterialName().toLowerCase())
                    .specific(specific + " " + parseOrder.getMaterialAdditionalSpecific())
                    .packaging(parseOrder.getMaterialPackaging())
                    .cost(price)
                    .source(SourceCompanies.MILE.toString())
                    .build();
        } catch (IOException e) {
            log.error("IOException: {}", e.getMessage());
            throw new RuntimeException(e);
        } catch (IndexOutOfBoundsException e) {
            log.error("Unable to parse page - required elements are missing: {} : {}", parseOrder.getMaterialName(),
                    parseOrder.getUrl());
            return null;
        } catch (RuntimeException e) {
            log.error("Unidentified error while parsing the page: {} : {}", parseOrder.getMaterialName(),
                    parseOrder.getUrl());
            return null;
        }
    }

}

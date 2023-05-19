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
public class By7745ParseService implements ParseService {

    private final ParseOrderService parseOrderService;

    @Override
    public List<MaterialDto> startParse() {
        log.info("Start parsing 7745");
        var orders = parseOrderService.prepareParseOrdersAndCheckForContent("7745.txt");
        List<MaterialDto> materials = new LinkedList<>();
        for (var order : orders) {
            val material = parseMaterialFrom7745(order);
            if (material != null) materials.add(material);
        }
        if (materials.isEmpty()) {
            log.warn(this.getClass().getName() + " : No orders for parse");
        }
        return materials;
    }

    private MaterialDto parseMaterialFrom7745(ParseOrder parseOrder) {
        try {
            val doc = Jsoup.connect(parseOrder.getUrl()).get();
            val priceText = doc.select("span.product__price-value").get(0).text();
            val price =
                    BigDecimal.valueOf(Double.parseDouble(priceText.substring(0, priceText.length() - 6)
                            .replace(',', '.')) + parseOrder.getCostModifier());
            val specific = doc.select("p.product-info-part__product-title").get(0).text();
            return MaterialDto.builder()
                    .name(parseOrder.getMaterialName().toLowerCase())
                    .specific(specific + " " + parseOrder.getMaterialAdditionalSpecific())
                    .packaging(parseOrder.getMaterialPackaging())
                    .cost(price)
                    .source(SourceCompanies.BY7745.toString())
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

package by.kaminsky.service.parse;

import by.kaminsky.dto.MaterialDto;
import by.kaminsky.enums.SourceCompanies;
import by.kaminsky.service.ParseOrderService;
import by.kaminsky.utils.ParseOrder;
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
public class Kaminov100ParseService implements ParseService {

    private final ParseOrderService parseOrderService;

    @Override
    public List<MaterialDto> startParse() {
        log.info("Start parsing 100kaminov");
        var orders = parseOrderService.prepareParseOrdersAndCheckForContent("100kaminov.txt");
        List<MaterialDto> materials = new LinkedList<>();
        for (var order : orders) {
            val material = parseMaterialFrom100Kaminov(order);
            if (material != null) materials.add(material);
        }
        if (materials.isEmpty()) {
            log.warn(this.getClass().getName() + " : No orders for parse");
        }
        return materials;
    }

    private MaterialDto parseMaterialFrom100Kaminov(ParseOrder parseOrder) {
        try {
            val doc = Jsoup.connect(parseOrder.getUrl()).get();
//            val elements = doc.select("span[data-qaid=product_name]");
//            for (var i = 0; i < elements.size(); i++) {
//                log.warn(elements.get(i).toString() + " // ");
//            }
            val price = BigDecimal.valueOf(Double.parseDouble(doc.select("span[data-qaid=product_price]")
                    .get(0).text().replace(',','.')) + parseOrder.getCostModifier());
            val specific = doc.select("span[data-qaid=product_name]").get(0).text();
            return MaterialDto.builder()
                    .name(parseOrder.getMaterialName().toLowerCase())
                    .specific(specific + " " + parseOrder.getMaterialAdditionalSpecific())
                    .packaging(parseOrder.getMaterialPackaging())
                    .cost(price)
                    .source(SourceCompanies.OGONBY.toString())
                    .build();
        } catch (IOException e) {
            log.error("IOException: {}", e.getMessage());
            throw new RuntimeException(e);
        } catch (IndexOutOfBoundsException e){
            log.error("Unable to parse page - required elements are missing: {} : {}", parseOrder.getMaterialName(),
                    parseOrder.getUrl() );
            return null;
        } catch (RuntimeException e) {
            log.error("Unidentified error while parsing the page: {} : {}", parseOrder.getMaterialName(),
                    parseOrder.getUrl() );
            return null;
        }
    }

}

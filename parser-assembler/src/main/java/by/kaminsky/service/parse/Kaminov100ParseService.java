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
import java.util.ArrayList;
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
        var orders = parseOrderService.prepareParseOrdersAndCheckForContent("parse_orders/100kaminov/100kaminov.txt");
        var ordersBlackTubes
                = parseOrderService.prepareParseOrdersAndCheckForContent("parse_orders/100kaminov/100kaminov_black_tubes.txt");
        List<MaterialDto> materials = new LinkedList<>();
        for (var order : orders) {
            val material = parseMaterialFrom100Kaminov(order);
            if (material != null) materials.add(material);
        }
        for (var order : ordersBlackTubes) {
            materials.addAll(parseBlackTubesFrom100Kaminov(order));
        }
        if (materials.isEmpty()) log.warn(this.getClass().getName() + " : No orders for parse");
        return materials;
    }

    private MaterialDto parseMaterialFrom100Kaminov(ParseOrder parseOrder) {
        try {
            val doc = Jsoup.connect(parseOrder.getUrl()).get();
            val price = BigDecimal.valueOf(Double.parseDouble(doc.select("span[data-qaid=product_price]")
                    .get(0).text().replace(',', '.')) + parseOrder.getCostModifier());
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

    private List<MaterialDto> parseBlackTubesFrom100Kaminov(ParseOrder parseOrder) {
        try {
            val doc = Jsoup.connect(parseOrder.getUrl()).get();
            val scriptWithUrls = doc.select("script").get(20).toString();
            log.debug("Source URL: {}", parseOrder.getUrl());
            val allMaterialUrls = extractUrlsFromScript(scriptWithUrls);
            var keyElements = new ArrayList<String>();
            keyElements.add("д120");
            keyElements.add("д130");
            keyElements.add("д150");
            keyElements.add("д180");
            keyElements.add("д200");
            var result = new LinkedList<MaterialDto>();
            for (var i = 0; i < allMaterialUrls.size(); i++) {
                val localDoc = Jsoup.connect(allMaterialUrls.get(i)).get();
                val price = BigDecimal.valueOf(Double.parseDouble(localDoc.select("span[data-qaid=product_price]")
                        .get(0).text().replace(',', '.')) + parseOrder.getCostModifier());
                val specific = localDoc.select("span[data-qaid=product_name]").get(0).text();
                var material = MaterialDto.builder()
                        .name(parseOrder.getMaterialName().toLowerCase() + " " + keyElements.get(i))
                        .specific(specific + (i == 0 ? " 120 " : " ") + parseOrder.getMaterialAdditionalSpecific())
                        .packaging(parseOrder.getMaterialPackaging())
                        .cost(price)
                        .source(SourceCompanies.OGONBY.toString())
                        .build();
                result.add(material);
            }
            return result;
        } catch (IOException e) {
            log.error("IOException: {}", e.getMessage());
            throw new RuntimeException(e);
        } catch (IndexOutOfBoundsException e) {
            log.error("Unable to parse page - required elements are missing: {} : {}", parseOrder.getMaterialName(),
                    parseOrder.getUrl());
            return new ArrayList<>();
        } catch (RuntimeException e) {
            log.error("Unidentified error while parsing the page: {} : {}", parseOrder.getMaterialName(),
                    parseOrder.getUrl());
            return new ArrayList<>();
        }
    }

    private List<String> extractUrlsFromScript(String script) {
        List<String> result = new ArrayList<>();
        for (String element : script.split(",")) {
            if (element.startsWith(" \\\"url\\\":")) {
                result.add(element.substring(12, element.length() - 2));
            }
        }
        log.debug("Extracted url's for parse: {}", result);
        return result;
    }

}
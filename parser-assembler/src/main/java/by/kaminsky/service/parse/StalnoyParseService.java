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
public class StalnoyParseService implements ParseService {

    private final ParseOrderService parseOrderService;

    @Override
    public List<MaterialDto> startParse() {
        log.info("Start parsing stalnoy");
        var orders = parseOrderService.prepareParseOrdersAndCheckForContent("parse_orders/stalnoy.txt");
        List<MaterialDto> materials = new LinkedList<>(parseSheetsFromStalnoy(orders.get(0)));
        for (var i = 1; i < orders.size(); i++) {
            materials.addAll(parseMaterialsFromStalnoy(orders.get(i)));
        }
        if (materials.isEmpty()) log.warn(this.getClass().getName() + " : No orders for parse");
        return materials;
    }

    private List<MaterialDto> parseMaterialsFromStalnoy(ParseOrder parseOrder) {
        try {
            val doc = Jsoup.connect(parseOrder.getUrl()).get();
            val elements = doc.select("table.datatable > tbody > tr > td");
            List<String> descriptions = new ArrayList<>();
            List<Double> priceElems = new ArrayList<>();
            for (var i = 0; i < elements.size(); i += 5) {
                if (i == 0) continue;
                if (elements.get(i + 4).text().equals("-")) continue;
                descriptions.add(elements.get(i).text());
                priceElems.add(Double.parseDouble(elements.get(i + 4).text().replace(',', '.')));
            }
            List<MaterialDto> result = new LinkedList<>();
            for (var i = 0; i < Math.min(descriptions.size(), priceElems.size()); i++) {
                result.add(MaterialDto.builder()
                        .name(parseOrder.getMaterialName().toLowerCase() + " " +
                                descriptions.get(i).substring(0, descriptions.get(i).indexOf('х')))
                        .specific(parseOrder.getMaterialAdditionalSpecific() + " " + descriptions.get(i))
                        .packaging(parseOrder.getMaterialPackaging())
                        .cost(BigDecimal.valueOf(priceElems.get(i) + parseOrder.getCostModifier()))
                        .source(SourceCompanies.STALNOY.toString())
                        .build());
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


    private List<MaterialDto> parseSheetsFromStalnoy(ParseOrder parseOrder) {
        try {
            val doc = Jsoup.connect(parseOrder.getUrl()).get();
            val elements = doc.select("table.datatable > tbody > tr > td");
            val headlines = doc.select("h4");
            List<String> descriptions = new ArrayList<>();
            List<Double> priceElems = new ArrayList<>();
            var tablesCounter = -1;
            for (var i = 0; i < elements.size(); i += 6) {
                if (tablesCounter > 1) break;
                if (elements.get(i).text().startsWith("Размер")) {
                    tablesCounter++;
                    continue;
                }
                descriptions.add(removeStrongTag(headlines.get(tablesCounter).text()) + " " + elements.get(i).text() +
                        " " + elements.get(i + 2).text() + " кг.");
                priceElems.add(Double.parseDouble(elements.get(i + 5).text().replace(',', '.')));
            }
            List<MaterialDto> result = new LinkedList<>();
            for (var i = 0; i < Math.min(descriptions.size(), priceElems.size()); i++) {
                result.add(MaterialDto.builder()
                        .name(parseOrder.getMaterialName().toLowerCase())
                        .specific(descriptions.get(i) + " " + parseOrder.getMaterialAdditionalSpecific())
                        .packaging(parseOrder.getMaterialPackaging())
                        .cost(BigDecimal.valueOf(priceElems.get(i) + parseOrder.getCostModifier()))
                        .source(SourceCompanies.STALNOY.toString())
                        .build());
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

    private String removeStrongTag(String string) {
        return (string.startsWith("<strong>") ? string.substring(8, string.length() - 9) : string);

    }

}

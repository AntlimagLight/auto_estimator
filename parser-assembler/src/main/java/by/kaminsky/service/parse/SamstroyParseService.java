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
public class SamstroyParseService implements ParseService {

    private final ParseOrderService parseOrderService;

    @Override
    public List<MaterialDto> startParse() {
        log.info("Start parsing samstroy");
        var orders = parseOrderService.prepareParseOrdersAndCheckForContent("samstroy.txt");
        List<MaterialDto> materials = new LinkedList<>();
        for (var order : orders) {
            materials.addAll(parseMaterialsFromSamstroy(order));
        }
        if (materials.isEmpty()) {
            log.warn(this.getClass().getName() + " : No orders for parse");
        }
        return materials;
    }

    private List<MaterialDto> parseMaterialsFromSamstroy(ParseOrder parseOrder) {
        try {
            val doc = Jsoup.connect(parseOrder.getUrl()).get();
            val PriceElems = doc.select("p.ci-price__price > span");
            val TextElems = doc.select("a.ci-info__menuitem");
            List<MaterialDto> result = new LinkedList<>();
            for (var i = 0; i < Math.min(PriceElems.size(), TextElems.size()); i++) {
                val textElement = TextElems.get(i).text();
                result.add(MaterialDto.builder()
                        .name(parseOrder.getMaterialName().toLowerCase() + addNameModifierIfRequire(textElement))
                        .specific(textElement + " " + parseOrder.getMaterialAdditionalSpecific())
                        .packaging(parseOrder.getMaterialPackaging())
                        .cost(BigDecimal.valueOf(Double.parseDouble(PriceElems.get(i).text()
                                .replace(',', '.')) + parseOrder.getCostModifier()))
                        .source(SourceCompanies.SAMSTROY.toString())
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

    private String addNameModifierIfRequire(String materialName) {
        return (materialName.endsWith("400×100×240") || materialName.endsWith("510×120×240") ? " перегородочный" : "") +
                (materialName.endsWith("ША-8") ? " шамотный" : "") +
                (materialName.startsWith("Кирпич силикатный") ? " силикатный" : "");

    }

}

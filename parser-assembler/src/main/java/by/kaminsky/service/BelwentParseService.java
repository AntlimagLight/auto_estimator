package by.kaminsky.service;

import by.kaminsky.dto.MaterialDto;
import by.kaminsky.utils.PageElements;
import by.kaminsky.utils.ParseOrder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class BelwentParseService implements ParseService {

    private final ParseOrderService parseOrderService;

    @Override
    public List<MaterialDto> startParse() {
        log.info("Start parsing belwent");
        var ordersRoundTubes = parseOrderService.prepareParseOrdersAndCheckForContent("belwent_round.txt");
        var ordersEllipseTubes = parseOrderService.prepareParseOrdersAndCheckForContent("belwent_ellipse.txt");
        List<MaterialDto> materials = new LinkedList<>();
        for (var order : ordersRoundTubes) {
            materials.addAll(parseRoundBareTubes(order));
        }
        for (var order : ordersEllipseTubes) {
            materials.addAll(parseEllipseBareTubes(order));
        }
        if (materials.isEmpty()) {
            log.warn(this.getClass().getName() + " : No orders for parse");
        }
        return materials;
    }

    private MaterialDto parseMaterialFromBelwent(ParseOrder parseOrder) {
        //            for (var elem: elements) {
        //                log.warn(elem.val() + " / " + elem.text());
        //            }
        return null;
    }

    private List<MaterialDto> parseRoundBareTubes(ParseOrder parseOrder) {
        try {
            val doc = Jsoup.connect(parseOrder.getUrl()).get();
            val pageElements = extractPageElements(doc);
            val basePrice = Double.parseDouble(doc.select("span.shk-price").get(0).text()) *
                    pageElements.getPriceElements().get(0) *
                    pageElements.getPriceElements().get(17) *
                    pageElements.getPriceElements().get(22);
            Map<Integer, String> keyElements = new HashMap<>();
            keyElements.put(4, "д120");
            keyElements.put(7, "д150");
            keyElements.put(9, "д180");
            keyElements.put(10, "д200");
            keyElements.put(12, "д250");
            keyElements.put(14, "д300");
            List<MaterialDto> result = new LinkedList<>();
            for (Map.Entry<Integer, String> entry : keyElements.entrySet()) {
                result.add(new MaterialDto(parseOrder.getMaterialName() + " " + entry.getValue(),
                        parseOrder.getMaterialAdditionalSpecific() + " " + pageElements.getTexts().get(entry.getKey()) + ", "
                                + pageElements.getTexts().get(22), parseOrder.getMaterialPackaging(),
                        BigDecimal.valueOf(basePrice * pageElements.getPriceElements().get(entry.getKey())
                                + parseOrder.getCostModifier()).setScale(2, RoundingMode.UP)));
            }
            return result;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private List<MaterialDto> parseEllipseBareTubes(ParseOrder parseOrder) {
        try {
            val doc = Jsoup.connect(parseOrder.getUrl()).get();
            val pageElements = extractPageElements(doc);
            val basePrice = Double.parseDouble(doc.select("span.shk-price").get(0).text()) *
                    pageElements.getPriceElements().get(0) *
                    pageElements.getPriceElements().get(7) *
                    pageElements.getPriceElements().get(12);
            Map<Integer, String> keyElements = new HashMap<>();
            keyElements.put(1, "д100/200");
            keyElements.put(2, "д110/230");
            keyElements.put(3, "д120/230");
            keyElements.put(4, "д130/240");
            List<MaterialDto> result = new LinkedList<>();
            for (Map.Entry<Integer, String> entry : keyElements.entrySet()) {
                result.add(new MaterialDto(parseOrder.getMaterialName() + " " + entry.getValue(),
                        parseOrder.getMaterialAdditionalSpecific() + " " + pageElements.getTexts().get(entry.getKey()) + ", "
                                + pageElements.getTexts().get(12), parseOrder.getMaterialPackaging(),
                        BigDecimal.valueOf(basePrice * pageElements.getPriceElements().get(entry.getKey()) + parseOrder.getCostModifier())
                                .setScale(2, RoundingMode.UP)));
            }
            return result;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private PageElements extractPageElements(Document document) {
        val elements = document.select("option");
        val priceElements = elements.stream()
                .map(element -> {
                    var string = element.val();
                    return Double.parseDouble(string.substring(string.indexOf('*') + 1));
                }).toList();
        val texts = elements.stream().map(Element::text).toList();
        for (var i = 0; i < elements.size(); i++) {
            log.info(priceElements.get(i).toString() + " // " + texts.get(i));
        }
        return new PageElements(priceElements, texts);
    }


}

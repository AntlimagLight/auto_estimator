package by.kaminsky.service.parse;

import by.kaminsky.dto.MaterialDto;
import by.kaminsky.enums.SourceCompanies;
import by.kaminsky.service.ParseOrderService;
import by.kaminsky.helper_objects.PageElements;
import by.kaminsky.helper_objects.ParseOrder;
import by.kaminsky.util.MaterialUtils;
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
        var ordersRoundTubes = parseOrderService.prepareParseOrdersAndCheckForContent("belwent/belwent_round.txt");
        var ordersEllipseTubes = parseOrderService.prepareParseOrdersAndCheckForContent("belwent/belwent_ellipse.txt");
        var ordersInsulatedTubes = parseOrderService.prepareParseOrdersAndCheckForContent("belwent/belwent_insulated.txt");
        var ordersBlackTubes = parseOrderService.prepareParseOrdersAndCheckForContent("belwent/belwent_black.txt");
        var ordersFasteners = parseOrderService.prepareParseOrdersAndCheckForContent("belwent/belwent_fasteners.txt");
        var ordersSchiedelIsokern = parseOrderService.prepareParseOrdersAndCheckForContent("belwent/schiedel_isokern.txt");
        var additionalMaterials = parseOrderService.prepareParseOrdersAndCheckForContent("belwent/additional_materials.txt");
        List<MaterialDto> materials = new LinkedList<>();
        for (var order : ordersRoundTubes) {
            materials.addAll(parseRoundBareTubes(order));
        }
        for (var order : ordersEllipseTubes) {
            materials.addAll(parseEllipseBareTubes(order));
        }
        for (var order : ordersInsulatedTubes) {
            materials.addAll(parseInsulatedTubes(order));
        }
        for (var order : ordersBlackTubes) {
            materials.addAll(parseBlackTubes(order));
        }
        for (var order : ordersFasteners) {
            materials.addAll(parseFasteners(order));
        }
        for (var order : ordersSchiedelIsokern) {
            val material = parseMaterialFromSchiedelIsokern(order);
            if (material != null) materials.add(material);
        }
        materials.addAll(MaterialUtils.convertOrdersToMaterials(additionalMaterials, SourceCompanies.BELWENT));
        if (materials.isEmpty()) {
            log.warn(this.getClass().getName() + " : No orders for parse");
        }
        return materials;
    }

    private MaterialDto parseMaterialFromSchiedelIsokern(ParseOrder parseOrder) {
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
                    .source(SourceCompanies.BELWENT_KERAM.toString())
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
                result.add(MaterialDto.builder()
                        .name(parseOrder.getMaterialName().toLowerCase() + " " + entry.getValue())
                        .specific(parseOrder.getMaterialAdditionalSpecific() + " "
                                + pageElements.getTexts().get(entry.getKey()) + " " + pageElements.getTexts().get(22))
                        .packaging(parseOrder.getMaterialPackaging())
                        .cost(BigDecimal.valueOf(basePrice * pageElements.getPriceElements().get(entry.getKey())
                                + parseOrder.getCostModifier()).setScale(2, RoundingMode.UP))
                        .source(SourceCompanies.BELWENT.toString())
                        .build());
            }
            return result;
        } catch (IOException e) {
            log.error("IOException: {}", e.getMessage());
            throw new RuntimeException(e);
        } catch (IndexOutOfBoundsException e) {
            log.error("Unable to parse page - required elements are missing: {} : {}", parseOrder.getMaterialName(),
                    parseOrder.getUrl());
            return new LinkedList<>();
        } catch (RuntimeException e) {
            log.error("Unidentified error while parsing the page: {} : {}", parseOrder.getMaterialName(),
                    parseOrder.getUrl());
            return new LinkedList<>();
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
                result.add(MaterialDto.builder()
                        .name(parseOrder.getMaterialName().toLowerCase() + " " + entry.getValue())
                        .specific(parseOrder.getMaterialAdditionalSpecific() + " "
                                + pageElements.getTexts().get(entry.getKey()) + " " + pageElements.getTexts().get(12))
                        .packaging(parseOrder.getMaterialPackaging())
                        .cost(BigDecimal.valueOf(basePrice * pageElements.getPriceElements().get(entry.getKey()) +
                                parseOrder.getCostModifier()).setScale(2, RoundingMode.UP))
                        .source(SourceCompanies.BELWENT.toString())
                        .build());
            }
            return result;
        } catch (IOException e) {
            log.error("IOException: {}", e.getMessage());
            throw new RuntimeException(e);
        } catch (IndexOutOfBoundsException e) {
            log.error("Unable to parse page - required elements are missing: {} : {}", parseOrder.getMaterialName(),
                    parseOrder.getUrl());
            return new LinkedList<>();
        } catch (RuntimeException e) {
            log.error("Unidentified error while parsing the page: {} : {}", parseOrder.getMaterialName(),
                    parseOrder.getUrl());
            return new LinkedList<>();
        }
    }

    private List<MaterialDto> parseInsulatedTubes(ParseOrder parseOrder) {
        try {
            val doc = Jsoup.connect(parseOrder.getUrl()).get();
            val pageElements = extractPageElements(doc);
            val basePrice = Double.parseDouble(doc.select("span.shk-price").get(0).text()) *
                    pageElements.getPriceElements().get(16) *
                    pageElements.getPriceElements().get(20) *
                    pageElements.getPriceElements().get(24) *
                    pageElements.getPriceElements().get(25) *
                    pageElements.getPriceElements().get(29);
            Map<Integer, String> keyElements = new HashMap<>();
            keyElements.put(3, "д120");
            keyElements.put(6, "д150");
            keyElements.put(8, "д180");
            keyElements.put(9, "д200");
            keyElements.put(11, "д250");
            keyElements.put(13, "д300");
            List<MaterialDto> result = new LinkedList<>();
            for (Map.Entry<Integer, String> entry : keyElements.entrySet()) {
                val textDiam = pageElements.getTexts().get(entry.getKey());
                val externalDiam = 100 + Integer.parseInt(textDiam.substring(textDiam.indexOf('=') + 1));
                result.add(MaterialDto.builder()
                        .name(parseOrder.getMaterialName().toLowerCase() + " " + entry.getValue())
                        .specific(parseOrder.getMaterialAdditionalSpecific() + " " + textDiam + "/" + externalDiam + " "
                                + pageElements.getTexts().get(24))
                        .packaging(parseOrder.getMaterialPackaging())
                        .cost(BigDecimal.valueOf(basePrice * pageElements.getPriceElements().get(entry.getKey())
                                + parseOrder.getCostModifier()).setScale(2, RoundingMode.UP))
                        .source(SourceCompanies.BELWENT.toString())
                        .build());
            }
            return result;
        } catch (IOException e) {
            log.error("IOException: {}", e.getMessage());
            throw new RuntimeException(e);
        } catch (IndexOutOfBoundsException e) {
            log.error("Unable to parse page - required elements are missing: {} : {}", parseOrder.getMaterialName(),
                    parseOrder.getUrl());
            return new LinkedList<>();
        } catch (RuntimeException e) {
            log.error("Unidentified error while parsing the page: {} : {}", parseOrder.getMaterialName(),
                    parseOrder.getUrl());
            return new LinkedList<>();
        }
    }

    @SuppressWarnings("DuplicatedCode")
    private List<MaterialDto> parseBlackTubes(ParseOrder parseOrder) {
        try {
            val doc = Jsoup.connect(parseOrder.getUrl()).get();
            val pageElements = extractPageElements(doc);
            val basePrice = Double.parseDouble(doc.select("span.shk-price").get(0).text()) *
                    pageElements.getPriceElements().get(0);
            Map<Integer, String> keyElements = new HashMap<>();
            keyElements.put(1, "д120");
            keyElements.put(2, "д150");
            keyElements.put(3, "д200");
            keyElements.put(4, "д250");
            List<MaterialDto> result = new LinkedList<>();
            for (Map.Entry<Integer, String> entry : keyElements.entrySet()) {
                result.add(MaterialDto.builder()
                        .name(parseOrder.getMaterialName().toLowerCase() + " " + entry.getValue())
                        .specific(parseOrder.getMaterialAdditionalSpecific() + " "
                                + pageElements.getTexts().get(entry.getKey()))
                        .packaging(parseOrder.getMaterialPackaging())
                        .cost(BigDecimal.valueOf(basePrice * pageElements.getPriceElements().get(entry.getKey())
                                + parseOrder.getCostModifier()).setScale(2, RoundingMode.UP))
                        .source(SourceCompanies.BELWENT.toString())
                        .build());
            }
            return result;
        } catch (IOException e) {
            log.error("IOException: {}", e.getMessage());
            throw new RuntimeException(e);
        } catch (IndexOutOfBoundsException e) {
            log.error("Unable to parse page - required elements are missing: {} : {}", parseOrder.getMaterialName(),
                    parseOrder.getUrl());
            return new LinkedList<>();
        } catch (RuntimeException e) {
            log.error("Unidentified error while parsing the page: {} : {}", parseOrder.getMaterialName(),
                    parseOrder.getUrl());
            return new LinkedList<>();
        }
    }

    private List<MaterialDto> parseFasteners(ParseOrder parseOrder) {
        try {
            val doc = Jsoup.connect(parseOrder.getUrl()).get();
            val pageElements = extractPageElements(doc);
            val basePrice = Double.parseDouble(doc.select("span.shk-price").get(0).text()) *
                    pageElements.getPriceElements().get(0);
            Map<Integer, String> keyElements = new HashMap<>();
            keyElements.put(4, "д120");
            keyElements.put(7, "д150");
            keyElements.put(9, "д180");
            keyElements.put(10, "д200");
            keyElements.put(12, "д250");
            keyElements.put(14, "д300");
            List<MaterialDto> result = new LinkedList<>();
            for (Map.Entry<Integer, String> entry : keyElements.entrySet()) {
                result.add(MaterialDto.builder()
                        .name(parseOrder.getMaterialName().toLowerCase() + " " + entry.getValue())
                        .specific(parseOrder.getMaterialAdditionalSpecific() + " "
                                + pageElements.getTexts().get(entry.getKey()))
                        .packaging(parseOrder.getMaterialPackaging())
                        .cost(BigDecimal.valueOf(basePrice * pageElements.getPriceElements().get(entry.getKey())
                                + parseOrder.getCostModifier()).setScale(2, RoundingMode.UP))
                        .source(SourceCompanies.BELWENT.toString())
                        .build());
            }
            return result;
        } catch (IOException e) {
            log.error("IOException: {}", e.getMessage());
            throw new RuntimeException(e);
        } catch (IndexOutOfBoundsException e) {
            log.error("Unable to parse page - required elements are missing: {} : {}", parseOrder.getMaterialName(),
                    parseOrder.getUrl());
            return new LinkedList<>();
        } catch (RuntimeException e) {
            log.error("Unidentified error while parsing the page: {} : {}", parseOrder.getMaterialName(),
                    parseOrder.getUrl());
            return new LinkedList<>();
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
            log.trace(priceElements.get(i).toString() + " // " + texts.get(i));
        }
        return new PageElements(priceElements, texts);
    }

}

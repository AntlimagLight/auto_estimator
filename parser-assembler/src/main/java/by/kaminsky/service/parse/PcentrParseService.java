package by.kaminsky.service.parse;

import by.kaminsky.dto.MaterialDto;
import by.kaminsky.enums.SourceCompanies;
import by.kaminsky.exception.NotPriceException;
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
public class PcentrParseService implements ParseService {

    private final ParseOrderService parseOrderService;

    @Override
    public List<MaterialDto> startParse() {
        log.info("Start parsing pcentr");
        var orders = parseOrderService.prepareParseOrdersAndCheckForContent("parse_orders/pcentr.txt");
        List<MaterialDto> materials = new LinkedList<>();
        for (var order : orders) {
            val material = parseMaterialFromPcentr(order);
            if (material != null) materials.add(material);
        }
        if (materials.isEmpty()) log.warn(this.getClass().getName() + " : No orders for parse");
        return materials;
    }

    private MaterialDto parseMaterialFromPcentr(ParseOrder parseOrder) {
        try {
            val doc = Jsoup.connect(parseOrder.getUrl()).get();
            val specific = doc.select("h1.byleft").text();
            val priceElement = doc.select("span.shk-price").text();
            if (priceElement.equals(".00")) throw new NotPriceException("Element have not price");
            val price = BigDecimal.valueOf(Double.parseDouble(priceElement.replace(',', '.'))
                    + parseOrder.getCostModifier());
            return MaterialDto.builder()
                    .name(parseOrder.getMaterialName().toLowerCase())
                    .specific(specific + " " + parseOrder.getMaterialAdditionalSpecific())
                    .packaging(parseOrder.getMaterialPackaging())
                    .cost(price)
                    .source(SourceCompanies.PECHNOYCENTR.toString())
                    .build();
        } catch (IOException e) {
            log.error("IOException: {}", e.getMessage());
            throw new RuntimeException(e);
        } catch (IndexOutOfBoundsException | NotPriceException e) {
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

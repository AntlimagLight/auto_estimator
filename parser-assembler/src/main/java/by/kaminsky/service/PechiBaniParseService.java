package by.kaminsky.service;

import by.kaminsky.dto.MaterialDto;
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
public class PechiBaniParseService implements ParseService {

    private final ParseOrderService parseOrderService;

    @Override
    public List<MaterialDto> startParse() {
        log.info("Start parsing pechibani");
        var orders = parseOrderService.prepareParseOrdersAndCheckForContent("pechibani.txt");
        List<MaterialDto> materials = new LinkedList<>();
        for (var order : orders) {
            materials.add(parseMaterialFromPechiBani(order));
        }
        if (materials.isEmpty()) {
            log.warn(this.getClass().getName() + " : No orders for parse");
        }
        return materials;
    }

    private MaterialDto parseMaterialFromPechiBani(ParseOrder parseOrder) {
        try {
            val doc = Jsoup.connect(parseOrder.getUrl()).get();
            val elements = doc.select("span.micro-price");
            val price = BigDecimal.valueOf(Double.parseDouble(elements.get(2).text()) +
                    parseOrder.getCostModifier());
            val specific = elements.get(0).text();
            return new MaterialDto(parseOrder.getMaterialName(), specific + " "
                    + parseOrder.getMaterialAdditionalSpecific(), parseOrder.getMaterialPackaging(), price);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}

package by.kaminsky.service;

import by.kaminsky.dto.MaterialDto;
import by.kaminsky.utils.ParseOrder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class BelwentParseService implements ParseService {
    @Override
    public List<MaterialDto> startParse() {
        log.info("Start parsing belwent");
//        var orders = prepareParseOrders();
//        var orders = new ArrayList<ParseOrder>();
        List<MaterialDto> materials = new LinkedList<>();
        materials.addAll(parseRoundBareTubes(new ParseOrder("труба", "Труба нерж. L=1000, ст.304",
                "шт.", "https://belwent.by/ksr10.html")));

//        if (orders == null) {
//            log.warn("No materials for parse");
//        } else {
//            for (var order : orders) {
//                materials.add(parseMaterialFromBelwent(order));
//            }
//        }

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
            val elements = doc.select("option");
            val priceElements = elements.stream()
                    .map(element -> {
                        var string = element.val();
                        return Double.parseDouble(string.substring(string.indexOf('*') + 1));
                    }).toList();
            val texts = elements.stream().map(Element::text).toList();
            val basePrice = Double.parseDouble(doc.select("span.shk-price").get(0).text()) *
                    priceElements.get(0) * priceElements.get(17) * priceElements.get(22);
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
                        parseOrder.getMaterialAdditionalSpecific() + " " + texts.get(entry.getKey()) + ", "
                                + texts.get(22), parseOrder.getMaterialPackaging(),
                        BigDecimal.valueOf(basePrice * priceElements.get(entry.getKey()))
                                .setScale(2, RoundingMode.FLOOR)));
            }
            return result;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

//    private List<ParseOrder> prepareParseOrders() {
//        List<ParseOrder> orders = new LinkedList<>();
//        Scanner scanner = null;
//        try {
//            String sep = File.separator;
//            File file = new File(Paths.get("parser-assembler" + sep + "src" + sep +
//                    "main" + sep + "resources" + sep + "pechibani.txt").toAbsolutePath().toString());
//            log.debug("Pechibani file path: " + file.getPath());
//            scanner = new Scanner(file);
//            var separatorLine = false;
//            do {
//                if (separatorLine) {
//                    log.info(">" + scanner.nextLine());
//                } else {
//                    separatorLine = true;
//                }
//                String[] lineBlock = new String[4];
//                for (var i = 0; i < lineBlock.length; i++) {
//                    lineBlock[i] = scanner.hasNext() ? scanner.nextLine() : null;
//                }
//                if (Arrays.stream(lineBlock).anyMatch(Objects::isNull)) break;
//                log.debug("Creating parse order: {}, {}, {}, {}", lineBlock[0], lineBlock[1], lineBlock[2], lineBlock[3]);
//                orders.add(new ParseOrder(lineBlock[0], lineBlock[1], lineBlock[2], lineBlock[3]));
//            } while (scanner.hasNext());
//            return orders;
//        } catch (IOException e) {
//            log.error("Exception while scanning a file {} {}", e.getClass(), e.getMessage());
//            return null;
//        } finally {
//            if (scanner != null) scanner.close();
//        }
//    }

}

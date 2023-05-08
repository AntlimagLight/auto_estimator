package by.kaminsky.service;

import by.kaminsky.dto.MaterialDto;
import by.kaminsky.utils.ParseOrder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Paths;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class PechiBaniParseService implements ParseService {
    @Override
    public List<MaterialDto> startParse() {
        log.info("Start parsing pechibani");
        var orders = prepareParseOrders();
        List<MaterialDto> materials = new LinkedList<>();
        if (orders == null) {
            log.warn("No materials for parse");
        } else {
            for (var order : orders) {
                materials.add(parseMaterialFromPechiBani(order));
            }
        }
        return materials;
    }

    private MaterialDto parseMaterialFromPechiBani(ParseOrder parseOrder) {
        try {
            val doc = Jsoup.connect(parseOrder.getUrl()).get();
            val elements = doc.select("span.micro-price");
            val price = BigDecimal.valueOf(Double.parseDouble(elements.get(2).text()));
            val specific = elements.get(0).text();
            return new MaterialDto(parseOrder.getMaterialName(), specific + " "
                    + parseOrder.getMaterialAdditionalSpecific(), parseOrder.getMaterialPackaging(), price);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private List<ParseOrder> prepareParseOrders() {
        List<ParseOrder> orders = new LinkedList<>();
        Scanner scanner = null;
        try {
            String sep = File.separator;
            File file = new File(Paths.get("parser-assembler" + sep + "src" + sep +
                    "main" + sep + "resources" + sep + "pechibani.txt").toAbsolutePath().toString());
            log.debug("Pechibani file path: " + file.getPath());
            scanner = new Scanner(file);
            var separatorLine = false;
            do {
                if (separatorLine) {
                    log.info(">" + scanner.nextLine());
                } else {
                    separatorLine = true;
                }
                String[] lineBlock = new String[4];
                for (var i = 0; i < lineBlock.length; i++) {
                    lineBlock[i] = scanner.hasNext() ? scanner.nextLine() : null;
                }
                if (Arrays.stream(lineBlock).anyMatch(Objects::isNull)) break;
                log.debug("Creating parse order: {}, {}, {}, {}", lineBlock[0], lineBlock[1], lineBlock[2], lineBlock[3]);
                orders.add(new ParseOrder(lineBlock[0], lineBlock[1], lineBlock[2], lineBlock[3]));
            } while (scanner.hasNext());
            return orders;
        } catch (IOException e) {
            log.error("Exception while scanning a file {} {}", e.getClass(), e.getMessage());
            return null;
        } finally {
            if (scanner != null) scanner.close();
        }
    }

}

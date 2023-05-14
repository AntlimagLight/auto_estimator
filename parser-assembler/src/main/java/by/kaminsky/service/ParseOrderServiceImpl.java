package by.kaminsky.service;

import by.kaminsky.utils.ParseOrder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class ParseOrderServiceImpl implements ParseOrderService {

    @Override
    public List<ParseOrder> prepareParseOrders(String fileName) {
        List<ParseOrder> orders = new LinkedList<>();
        Scanner scanner = null;
        try {
            String sep = File.separator;
            File file = new File(Paths.get("parser-assembler" + sep + "src" + sep +
                    "main" + sep + "resources" + sep + fileName).toAbsolutePath().toString());
            log.debug("Pechibani file path: " + file.getPath());
            scanner = new Scanner(file);
            var separatorLine = false;
            do {
                if (separatorLine) {
                    scanner.nextLine();
                } else {
                    separatorLine = true;
                }
                String[] lineBlock = new String[5];
                for (var i = 0; i < lineBlock.length; i++) {
                    lineBlock[i] = scanner.hasNext() ? scanner.nextLine() : null;
                }
                if (Arrays.stream(lineBlock).anyMatch(Objects::isNull)) break;
                log.info("Creating parse order: {}, {}, {}, {}, {}", lineBlock[0], lineBlock[1], lineBlock[2],
                        lineBlock[3], lineBlock[4]);
                Double addCost = lineBlock[2].equals("") ? 0 : Double.parseDouble(lineBlock[2]);
                orders.add(new ParseOrder(lineBlock[0], lineBlock[1], addCost, lineBlock[3], lineBlock[4]));
            } while (scanner.hasNext());
            return orders;
        } catch (IOException e) {
            log.error("Exception while scanning a file {} {}", e.getClass(), e.getMessage());
            return null;
        } finally {
            if (scanner != null) scanner.close();
        }
    }

    public List<ParseOrder> prepareParseOrdersAndCheckForContent(String fileName) {
        var orders = prepareParseOrders(fileName);
        if (orders == null || orders.isEmpty()) {
            log.warn(fileName + " --> No orders for parse");
            return new ArrayList<>();
        } else {
            return orders;
        }
    }

}

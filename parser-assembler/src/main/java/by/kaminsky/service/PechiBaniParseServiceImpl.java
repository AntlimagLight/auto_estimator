package by.kaminsky.service;

import by.kaminsky.dto.MaterialDto;
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
public class PechiBaniParseServiceImpl implements PechiBaniParseService {
    @Override
    public List<MaterialDto> startParse() {
        log.info("Start parsing pechibani");
        List<MaterialDto> materials = new LinkedList<>();
        materials.add(parseMaterialFromPechiBani("суперизол", "лист",
                "1200мм х 1000мм х 30мм",
                "https://pechibani.by/materialy-dlya-bani/plita-teploizolyacionnaya-superizol-skamol-daniya/"));
        materials.add(parseMaterialFromPechiBani("скотч", "шт.",
                "50мм х 5м",
                "https://pechibani.by/materialy-dlya-bani/izolyatsiya-i-uteplitel/skotch-alyuminievyy/"));
        materials.add(parseMaterialFromPechiBani("фольга", "м.п.",
                "50 микрон",
                "https://pechibani.by/materialy-dlya-bani/folga-alyuminievaya/"));
        return materials;
    }

    private MaterialDto parseMaterialFromPechiBani(String name, String packaging,
                                                   String addSpecific, String url) {
        try {
            val doc = Jsoup.connect(url).get();
            val elements = doc.select("span.micro-price");
            for (var elem : elements) {
                System.out.println("- " + elem.text());
            }
            val price = BigDecimal.valueOf(Double.parseDouble(elements.get(2).text()));
            val specific = elements.get(0).text();
            return new MaterialDto(name, specific + " " + addSpecific, packaging, price);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

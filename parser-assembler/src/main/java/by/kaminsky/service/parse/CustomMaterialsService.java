package by.kaminsky.service.parse;

import by.kaminsky.dto.MaterialDto;
import by.kaminsky.enums.SourceCompanies;
import by.kaminsky.service.ParseOrderService;
import by.kaminsky.util.MaterialUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomMaterialsService implements ParseService {

    private final ParseOrderService parseOrderService;

    @Override
    public List<MaterialDto> startParse() {
        log.info("Start adding custom materials");
        var orders = parseOrderService.prepareParseOrdersAndCheckForContent("kaminsky_own_matterials.txt");
        List<MaterialDto> materials =
                new LinkedList<>(MaterialUtils.convertOrdersToMaterials(orders, SourceCompanies.KAMINSKY));
        if (materials.isEmpty()) log.warn(this.getClass().getName() + " : No orders for add");
        return materials;
    }


}

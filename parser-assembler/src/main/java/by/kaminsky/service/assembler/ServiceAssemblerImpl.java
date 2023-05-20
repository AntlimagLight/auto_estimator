package by.kaminsky.service.assembler;

import by.kaminsky.service.parse.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ServiceAssemblerImpl implements ServiceAssembler {

    private final Map<ParseService, Boolean> services = new HashMap<>();

    @Autowired
    public ServiceAssemblerImpl(PechiBaniParseService pechiBaniParseService, BelwentParseService belwentParseService,
                                Kaminov100ParseService kaminov100ParseService, By7745ParseService by7745ParseService,
                                MileParseService mileParseService, SamstroyParseService samstroyParseService,
                                StalnoyParseService stalnoyParseService, PcentrParseService pcentrParseService,
                                CustomMaterialsService customMaterialsService) {
        services.put(pechiBaniParseService, true);
        services.put(belwentParseService, true);
        services.put(kaminov100ParseService, true);
        services.put(by7745ParseService, true);
        services.put(mileParseService, true);
        services.put(samstroyParseService, true);
        services.put(stalnoyParseService, true);
        services.put(pcentrParseService, true);
        services.put(customMaterialsService, true);
    }

    @Override
    public List<ParseService> getEnabledParsingServices() {
        return services.entrySet().stream()
                .filter(e -> e.getValue() == Boolean.TRUE)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    @Override
    public void disableParsingService(String serviceName) {
        var serviceOpt = services.entrySet().stream()
                .filter(e -> e.getKey().getClass().getName().endsWith(serviceName))
                .findFirst();
        if (serviceOpt.isPresent()) {
            services.put(serviceOpt.get().getKey(), false);
            log.info("Successfully disabled service : {}", serviceName);
        } else {
            log.warn("Service with this name is not found");
        }
    }

    @Override
    public void enableParsingService(String serviceName) {
        var serviceOpt = services.entrySet().stream()
                .filter(e -> e.getKey().getClass().getName().endsWith(serviceName))
                .findFirst();
        if (serviceOpt.isPresent()) {
            services.put(serviceOpt.get().getKey(), true);
            log.info("Successfully enabled service : {}", serviceName);
        } else {
            log.warn("Service with this name is not found");
        }
    }
}

package by.kaminsky.service;

import by.kaminsky.service.assembler.ServiceAssembler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
@Slf4j
public class MainParsingServiceImpl implements MainParsingService {

    private final ProducerService producerService;
    private final ServiceAssembler serviceAssembler;

    @Override
    @Scheduled(initialDelayString = "${parse.init.timeout}", fixedDelayString = "${parse.scheduled.timeout}")
    public void startParseAll() {
        log.info("Start parsing!");

//        serviceAssembler.disableParsingService("PechiBaniParseService");
//        serviceAssembler.disableParsingService("BelwentParseService");
//        serviceAssembler.disableParsingService("Kaminov100ParseService");
//        serviceAssembler.disableParsingService("By7745ParseService");
//        serviceAssembler.disableParsingService("MileParseService");
//        serviceAssembler.disableParsingService("SamstroyParseService");
//        serviceAssembler.disableParsingService("StalnoyParseService");
//        serviceAssembler.disableParsingService("PcentrParseService ");
//        serviceAssembler.disableParsingService("CustomMaterialsService");

        for (var service : serviceAssembler.getEnabledParsingServices()) {
            service.startParse().forEach(producerService::producerAnswer);
        }

    }
}

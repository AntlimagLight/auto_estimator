package by.kaminsky.service;

import by.kaminsky.service.parse.BelwentParseService;
import by.kaminsky.service.parse.Kaminov100ParseService;
import by.kaminsky.service.parse.PechiBaniParseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
@Slf4j
public class MainParsingServiceImpl implements MainParsingService {

    private final ProducerService producerService;

    private final PechiBaniParseService pechiBaniParseService;
    private final BelwentParseService belwentParseService;
    private final Kaminov100ParseService kaminov100ParseService;


    @Override
    @Scheduled(initialDelayString = "${parse.init.timeout}", fixedDelayString = "${parse.scheduled.timeout}")
    public void startParseAll() {
        log.info("Start parsing!");
        kaminov100ParseService.startParse().forEach(producerService::producerAnswer);
        pechiBaniParseService.startParse().forEach(producerService::producerAnswer);
        belwentParseService.startParse().forEach(producerService::producerAnswer);

    }
}

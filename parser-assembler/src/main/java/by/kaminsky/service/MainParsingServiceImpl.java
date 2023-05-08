package by.kaminsky.service;

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

    @Override
    @Scheduled(initialDelayString = "${parse.init.timeout}", fixedDelayString = "${parse.scheduled.timeout}")
    public void startParseAll() {
        log.info("Start parsing!");
//        pechiBaniParseService.startParse().forEach(producerService::producerAnswer);
        belwentParseService.startParse().forEach(producerService::producerAnswer);

    }
}

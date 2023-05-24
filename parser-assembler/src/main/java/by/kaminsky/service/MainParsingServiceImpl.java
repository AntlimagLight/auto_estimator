package by.kaminsky.service;

import by.kaminsky.exchangeRate.BynExchangeRate;
import by.kaminsky.repository.ExchangeRatesStorage;
import by.kaminsky.service.assembler.ServiceAssembler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;


@Service
@RequiredArgsConstructor
@Slf4j
public class MainParsingServiceImpl implements MainParsingService {

    private final ProducerService producerService;
    private final ServiceAssembler serviceAssembler;
    private final ExchangeRatesStorage exchangeRatesStorage;

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
//        serviceAssembler.disableParsingService("PcentrParseService");
//        serviceAssembler.disableParsingService("CustomMaterialsService");

        if (!exchangeRatesStorage.checkExchangeRate("USD")) return;

        for (var service : serviceAssembler.getEnabledParsingServices()) {
            service.startParse().stream()
                    .peek(materialDto -> materialDto.setCost(BigDecimal.valueOf(materialDto.getCost().doubleValue() /
                            exchangeRatesStorage.getRate("USD").getOfficialRate())))
                    .forEach(producerService::producerAnswer);
        }
        log.info("End parsing!");
    }


    @Scheduled(initialDelayString = "${exchange.init.timeout}", fixedDelayString = "${exchange.scheduled.timeout}")
    @Override
    public void updExchangeRates() {
        try {
            var rate = BynExchangeRate.requestRateUSD();
            log.info("got exchange rate {}", rate.toString());
            exchangeRatesStorage.setRate("USD", rate);
        } catch (Exception e) {
            log.error("Failed to get USD exchange rate");
        }
    }

}

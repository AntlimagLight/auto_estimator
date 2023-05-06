package by.kaminsky.service;

import by.kaminsky.dto.PrometheusRequestData;
import by.kaminsky.entity.Material;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;

import static by.kaminsky.constants.RabbitQueue.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class ConsumerServiceImpl implements ConsumerService {

    private final MainService mainService;

    @Override
    @RabbitListener(queues = TEXT_MESSAGE_UPDATE)
    public void consumeTextMessageUpdates(Update update) {
        log.info("NODE: massage consumed");
        mainService.processTextMessage(update);
    }

    @Override
    @RabbitListener(queues = PROMETHEUS_REQUEST_UPDATE)
    public void consumePrometheusRequest(PrometheusRequestData prometheusRequestData) {
        log.info("NODE: prometheus request consumed");
        mainService.processPrometheusRequest(prometheusRequestData);

    }

    @Override
    @RabbitListener(queues = NEW_MATERIALS)
    public void consumeNewMaterial(Material material) {
        log.info("NODE: new material " + material.getName() + " consumed");
        mainService.processSaveMaterialFromQueue(material);
    }

}

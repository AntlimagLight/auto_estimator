package by.kaminsky.service;

import by.kaminsky.dto.PrometheusRequestData;
import org.telegram.telegrambots.meta.api.objects.Update;

public interface ConsumerService {
    void consumeTextMessageUpdates(Update update);

    void consumePrometheusRequest(PrometheusRequestData prometheusRequestData);

}
package by.kaminsky.service;

import by.kaminsky.dto.PrometheusRequestData;
import org.telegram.telegrambots.meta.api.objects.Update;

public interface MainService {

    void processTextMessage(Update update);

    void processPrometheusRequest(PrometheusRequestData prometheusRequestData);
}

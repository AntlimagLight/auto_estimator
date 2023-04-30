package by.kaminsky.service;

import by.kaminsky.dialogsesions.DialogSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;

@Service
@Slf4j
@RequiredArgsConstructor
public class UpdateProducerImpl implements UpdateProducer {
    private final RabbitTemplate rabbitTemplate;

    @Override
    public void produce(String rabbitQueue, Update update) {
        log.info(rabbitQueue + " : " + update.getMessage().getText());
        rabbitTemplate.convertAndSend(rabbitQueue, update);
    }

    @Override
    public void produceSessionRequest(String rabbitQueue, DialogSession dialogSession) {
        log.info(rabbitQueue + " : " + dialogSession.getRequestData().getUpdate().getMessage().getText());
        rabbitTemplate.convertAndSend(rabbitQueue, dialogSession.getRequestData());
    }
}

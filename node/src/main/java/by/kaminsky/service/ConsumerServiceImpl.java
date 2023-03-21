package by.kaminsky.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import static by.kaminsky.model.RabbitQueue.TEXT_MESSAGE_UPDATE;

@Service
@Slf4j
@RequiredArgsConstructor
public class ConsumerServiceImpl implements ConsumerService {

    private final ProducerService producerService;

    @Override
    @RabbitListener(queues = TEXT_MESSAGE_UPDATE)
    public void consumeTextMessageUpdates(Update update) {
        log.info("NODE: Cообщение получено");

        testAnswer(update);

    }

    public void testAnswer(Update update) {
        var message = update.getMessage();
        var sendMassage = new SendMessage();
        sendMassage.setChatId(message.getChatId());
        sendMassage.setText("Hello From NODE");
        producerService.producerAnswer(sendMassage);
    }
}

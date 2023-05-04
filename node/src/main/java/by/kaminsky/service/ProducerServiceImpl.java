package by.kaminsky.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import static by.kaminsky.constants.RabbitQueue.ANSWER_MESSAGE;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProducerServiceImpl implements ProducerService {
    private final RabbitTemplate rabbitTemplate;

    @Override
    public void producerAnswer(SendMessage sendMessage) {
        rabbitTemplate.convertAndSend(ANSWER_MESSAGE, sendMessage);
    }

    public void generateTextAnswer(Update update, String text) {
        var message = update.getMessage();
        var sendMassage = new SendMessage();
        sendMassage.setChatId(message.getChatId());
        sendMassage.setText(text);
        producerAnswer(sendMassage);
    }
}

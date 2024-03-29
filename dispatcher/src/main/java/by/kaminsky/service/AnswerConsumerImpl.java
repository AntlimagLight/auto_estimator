package by.kaminsky.service;

import by.kaminsky.controller.UpdateController;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import static by.kaminsky.constants.RabbitQueue.ANSWER_MESSAGE;

@Service
@Slf4j
@RequiredArgsConstructor
public class AnswerConsumerImpl implements AnswerConsumer {

    private final UpdateController updateController;

    @Override
    @RabbitListener(queues = ANSWER_MESSAGE)
    public void consume(SendMessage sendMessage) {
        sendMessage.setParseMode(ParseMode.HTML);
        sendMessage.enableHtml(true);
        sendMessage.disableNotification();
        updateController.view(sendMessage);
    }
}

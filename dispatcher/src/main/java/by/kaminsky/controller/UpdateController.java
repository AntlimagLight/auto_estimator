package by.kaminsky.controller;

import by.kaminsky.service.UpdateProducer;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import static by.kaminsky.model.RabbitQueue.TEXT_MESSAGE_UPDATE;

@Component
@Slf4j
public class UpdateController {
    private TelegramBot telegramBot;
    private final UpdateProducer updateProducer;

    public void registerBot(TelegramBot telegramBot) {
        this.telegramBot = telegramBot;
    }

    public UpdateController(UpdateProducer updateProducer) {
        this.updateProducer = updateProducer;
    }

    public void processUpdate(Update update) {
        if (update == null) {
            log.warn("Received update is null");
            return;
        }
        val message = update.getMessage();
        if (message == null || message.getText() == null) {
            log.warn("Message type not supported: " + update);
            serviceResponse(update, "❗️Данный вид сообщения не поддерживается.");
        } else {
            processTextMessage(update);
        }
    }

    public void view(SendMessage sendMessage) {
        telegramBot.sendAnswerMessage(sendMessage);
    }

    private void processTextMessage(Update update) {
        updateProducer.produce(TEXT_MESSAGE_UPDATE, update);
        serviceResponse(update, "Идет обработка...");
    }

    public void serviceResponse(Update update, String text) {
        var response = new SendMessage();
        response.setChatId(update.getMessage().getChatId().toString());
        response.setText(text);
        view(response);
    }
}

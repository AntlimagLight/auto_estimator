package by.kaminsky.controller;

import by.kaminsky.dialogsesions.PrometheusSession;
import by.kaminsky.service.UpdateProducer;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.HashMap;
import java.util.Map;

import static by.kaminsky.constants.RabbitQueue.PROMETHEUS_REQUEST_UPDATE;
import static by.kaminsky.constants.RabbitQueue.TEXT_MESSAGE_UPDATE;

@Component
@Slf4j
public class UpdateController {
    private TelegramBot telegramBot;
    private final UpdateProducer updateProducer;

    private final Map<Long, PrometheusSession> prometheusChats = new HashMap<>();

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
            serviceResponse(update.getMessage().getChatId(), "❗️Данный вид сообщения не поддерживается.");
            return;
        }
        // Prometheus case
        val chatId = message.getChatId();
        if (prometheusChats.containsKey(chatId)) {
            val sessionResponse = prometheusChats.get(chatId).sessionProcess(message.getText());
            if (sessionResponse == null) {
                log.info("Отправляем данные по прометею: {}", prometheusChats.get(chatId).getRequestData().toString());
                updateProducer.produceSessionRequest(PROMETHEUS_REQUEST_UPDATE, prometheusChats.get(chatId));
                serviceResponse(update.getMessage().getChatId(), "Обрабатывается запрос на расчет стоимости...");
                prometheusChats.remove(chatId);
            } else {
                serviceResponse(chatId, sessionResponse);
            }
            return;
        } else if (message.getText().toLowerCase().contains("прометей")) {
            var newSession = new PrometheusSession(update);
            serviceResponse(chatId, newSession.sessionProcess(""));
            prometheusChats.put(chatId, newSession);
            return;
        }

        // Default case
        processTextMessage(update);
    }

    public void view(SendMessage sendMessage) {
        telegramBot.sendAnswerMessage(sendMessage);
    }

    private void processTextMessage(Update update) {
        updateProducer.produce(TEXT_MESSAGE_UPDATE, update);
        serviceResponse(update.getMessage().getChatId(), "Идет обработка...");
    }

    public void serviceResponse(Long chatId, String text) {
        var response = new SendMessage();
        response.setChatId(chatId.toString());
        response.setText(text);
        view(response);
    }
}

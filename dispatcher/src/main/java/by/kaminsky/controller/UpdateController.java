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
        if (!baseValidation(update)) {
            return;
        }

        val message = update.getMessage();
        val text = message.getText();
        val chatId = message.getChatId();

        // StopSessions case
        if (text.toLowerCase().contains("stop") || text.toLowerCase().contains("стоп")
                || text.toLowerCase().contains("отмена") || text.toLowerCase().contains("cancel")) {
            prometheusChats.remove(chatId);
            log.info("User sessions cleared {}", chatId);
            return;
        }

        // Prometheus case
        if (prometheusChats.containsKey(chatId)) {
            val sessionResponse = prometheusChats.get(chatId).sessionProcess(text);
            if (sessionResponse == null) {
                log.info("Sending data via Prometheus: {}", prometheusChats.get(chatId).getRequestData().toString());
                updateProducer.produceSessionRequest(PROMETHEUS_REQUEST_UPDATE, prometheusChats.get(chatId));
                serviceResponse(chatId, "Обрабатывается запрос на расчет стоимости...");
                prometheusChats.remove(chatId);
            } else {
                serviceResponse(chatId, sessionResponse);
            }
            return;
        } else if (text.toLowerCase().contains("прометей")) {
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

    private void serviceResponse(Long chatId, String text) {
        var response = new SendMessage();
        response.setChatId(chatId.toString());
        response.setText(text);
        view(response);
    }

    private boolean baseValidation(Update update) {
        if (update == null || !update.hasMessage()) {
            log.warn("Received update is null or not have message");
            return false;
        }
        if (!update.getMessage().hasText()) {
            log.warn("Message type not supported: " + update);
            serviceResponse(update.getMessage().getChatId(), "❗️Данный вид сообщения не поддерживается.");
            return false;
        }
        return true;
    }


    //TODO реализовать метод для чистки сессий по таймауту
}

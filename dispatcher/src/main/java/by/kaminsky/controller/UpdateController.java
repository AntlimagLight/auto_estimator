package by.kaminsky.controller;

import by.kaminsky.dialogsesions.PrometheusSession;
import by.kaminsky.service.EstimateCreationService;
import by.kaminsky.service.UpdateProducer;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static by.kaminsky.constants.RabbitQueue.PROMETHEUS_REQUEST_UPDATE;
import static by.kaminsky.constants.RabbitQueue.TEXT_MESSAGE_UPDATE;

@Component
@Slf4j
public class UpdateController {
    private TelegramBot telegramBot;
    private final UpdateProducer updateProducer;
    private final EstimateCreationService estimateCreationService;

    private final Map<Long, PrometheusSession> prometheusChats = new HashMap<>();

    public void registerBot(TelegramBot telegramBot) {
        this.telegramBot = telegramBot;
    }

    public UpdateController(UpdateProducer updateProducer, EstimateCreationService estimateCreationService) {
        this.updateProducer = updateProducer;
        this.estimateCreationService = estimateCreationService;
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

        // FileSend case
        if (text.toLowerCase().contains("file") || text.toLowerCase().contains("файл")) {
            log.info("FILE SEND CASE {}", chatId);
            val path = estimateCreationService.createPrometheusEstimate(estimateCreationService.createTest());
            if (path == null) {
                log.warn("Unable to send file");
                serviceResponse(update.getMessage().getChatId(), "❗️Возникла ошибка. Смета не может быть отправлена.");
            } else {
                sendFile(chatId, "test sending", path);
                val file = new File(path);
                if (file.delete()) {
                    log.info("File {} deleted successfully", file.getName());
                } else log.warn("File {} could not be deleted", file.getName());
            }
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
    }

    private void serviceResponse(Long chatId, String text) {
        var response = new SendMessage();
        response.setChatId(chatId.toString());
        response.setText(text);
        response.setParseMode(ParseMode.MARKDOWNV2);
        response.enableMarkdownV2(true);
        response.disableNotification();
        view(response);
    }

    private void sendFile(Long chatId, String text, String filePath) {
        File file = new File(filePath);
        var document = new SendDocument();
        document.setChatId(chatId);
        document.setDocument(new InputFile(file));
        document.setCaption(text);
        try {
            telegramBot.execute(document);
            log.info("File {} sent to user successfully", file.getName());
        } catch (TelegramApiException e) {
            log.error(e.getMessage());
            throw new RuntimeException(e);
        }


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

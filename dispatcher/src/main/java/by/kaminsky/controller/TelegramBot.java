package by.kaminsky.controller;

import by.kaminsky.config.BotConfig;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
@AllArgsConstructor
@Slf4j
public class TelegramBot extends TelegramLongPollingBot {

    private final BotConfig botConfig;

    @Override
    public String getBotUsername() {
        return botConfig.getBotName();
    }

    @SuppressWarnings("deprecation")
    @Override
    public String getBotToken() {
        return botConfig.getToken();
    }

    @Override
    public void onUpdateReceived(Update update) {
        var massage = update.getMessage();
        var text = massage.getText();
        var user  = massage.getFrom().getFirstName();
        log.info(user + " : " + text);

        var response = new SendMessage();
        response.setChatId(massage.getChatId().toString());
        response.setText("Hello from bot!");
        sendAnswerMessage(response);
    }

    public void sendAnswerMessage(SendMessage message) {
        if (message != null) {
            try {
                execute(message);
            } catch (TelegramApiException e) {
                log.error(e.getMessage());
            }
        }
    }
}

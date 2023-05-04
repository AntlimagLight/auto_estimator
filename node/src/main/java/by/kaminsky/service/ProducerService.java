package by.kaminsky.service;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

public interface ProducerService {
    void producerAnswer(SendMessage sendMessage);

    void generateTextAnswer(Update update, String text);
}

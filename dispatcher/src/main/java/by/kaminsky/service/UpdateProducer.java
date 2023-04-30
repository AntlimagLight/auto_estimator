package by.kaminsky.service;

import by.kaminsky.dialogsesions.DialogSession;
import org.telegram.telegrambots.meta.api.objects.Update;

public interface UpdateProducer {

    void produce(String rabbitQueue, Update update);

    void produceSessionRequest(String rabbitQueue, DialogSession dialogSession);
}

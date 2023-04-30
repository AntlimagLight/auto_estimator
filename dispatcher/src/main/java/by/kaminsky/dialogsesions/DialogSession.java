package by.kaminsky.dialogsesions;

import by.kaminsky.dto.RequestData;

public interface DialogSession {

    String sessionProcess(String userAnswer);

    RequestData getRequestData();
}

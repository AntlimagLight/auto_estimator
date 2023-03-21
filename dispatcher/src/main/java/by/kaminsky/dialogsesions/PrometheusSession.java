package by.kaminsky.dialogsesions;

import by.kaminsky.dto.PrometheusRequestData;
import lombok.Getter;
import lombok.val;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.ArrayList;
import java.util.List;

@Getter
public class PrometheusSession {

    private final long chatID;
    private int stage;

    private final PrometheusRequestData requestData;

    public PrometheusSession(Update update) {
        this.chatID = update.getMessage().getChatId();
        this.stage = 0;
        this.requestData = new PrometheusRequestData(update);
    }

    public String prometheusSessionProcess(String userAnswer) {
        return switch (stage) {
            case (0) -> stage0();
            case (1) -> stage1(userAnswer);
            case (2) -> stage2(userAnswer);
            case (3) -> stage3(userAnswer);
            case (4) -> stage4(userAnswer);
            case (5) -> stage5(userAnswer);
            case (6) -> stage6(userAnswer);
            default -> "Ошибка сессии Прометея";
        };
    }

    public String stage0() {
        stage++;
        return "Диаметр дымохода?";
    }

    public String stage1(String userAnswer) {
        List<Integer> validValues = new ArrayList<>();
        validValues.add(120);
        validValues.add(150);
        validValues.add(180);
        validValues.add(200);
        val diameter = Integer.parseInt(userAnswer);
        if (validValues.contains(diameter)) {
            requestData.setDiameter(diameter);
            stage++;
            return "Высота дымохода?";
        } else {
            return "Введено неверное значение? Попробуйте снова";
        }
    }

    public String stage2(String userAnswer) {
        val height = Integer.parseInt(userAnswer);
        requestData.setHeight(height);
        stage++;
        return "Нужна ли покрывная плита? да/нет";
    }

    public String stage3(String userAnswer) {
        switch (userAnswer.toLowerCase()) {
            case "да":
                requestData.setCoverPlate(true);
                stage++;
                return "Нужен ли зонт? да/нет";
            case "нет":
                requestData.setCoverPlate(false);
                stage++;
                return "Нужен ли зонт? да/нет";
            default:
                return "Введен неверный ответ, попробуйте снова";
        }
    }

    public String stage4(String userAnswer) {
        switch (userAnswer.toLowerCase()) {
            case "да":
                requestData.setUmbrella(true);
                stage++;
                return "Усиление шпильками по всей длинне? да/нет";
            case "нет":
                requestData.setUmbrella(false);
                stage++;
                return "Усиление шпильками по всей длинне? да/нет";
            default:
                return "Введен неверный ответ, попробуйте снова";
        }
    }

    public String stage5(String userAnswer) {
        switch (userAnswer.toLowerCase()) {
            case "да":
                requestData.setReinforcement(true);
                stage++;
                return "Необходим ли блок с вентканалом? да/нет";
            case "нет":
                requestData.setReinforcement(false);
                stage++;
                return "Необходим ли блок с вентканалом? да/нет";
            default:
                return "Введен неверный ответ, попробуйте снова";
        }
    }

    public String stage6(String userAnswer) {
        switch (userAnswer.toLowerCase()) {
            case "да":
                requestData.setVentilationDuct(true);
                stage++;
                return null;
            case "нет":
                requestData.setVentilationDuct(false);
                stage++;
                return null;
            default:
                return "Введен неверный ответ, попробуйте снова";
        }
    }
}

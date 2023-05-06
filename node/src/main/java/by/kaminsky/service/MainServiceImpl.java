package by.kaminsky.service;

import by.kaminsky.dto.PrometheusRequestData;
import by.kaminsky.entity.Material;
import by.kaminsky.enums.ServiceCommand;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@SuppressWarnings("SameParameterValue")
@Service
@RequiredArgsConstructor
@Slf4j
public class MainServiceImpl implements MainService {

    private final MaterialService materialService;

    private final ProducerService producerService;

    @Override
    public void processTextMessage(Update update) {
        val text = update.getMessage().getText();
        val separator = ' ';
        ServiceCommand serviceCommand;
        if (text.contains(Character.toString(separator)) && text.charAt(text.length() - 1) != separator) {
            String[] textAndPrefix = text.split(Character.toString(separator), 2);
            serviceCommand = ServiceCommand.fromValue(textAndPrefix[0]);
        } else {
            serviceCommand = ServiceCommand.fromValue(text);
        }
        switch (serviceCommand) {
            case START ->
                    producerService.generateTextAnswer(update, "Добро пожаловать! Напишите /Help, чтобы посмотреть" +
                            "список доступных команд");
            case HELP -> //TODO сделать список команд
                    producerService.generateTextAnswer(update, "Здесь будет список доступных команд!");
            case SAVE -> {
                try {
                    processSaveFromMassage(update, separator);
                } catch (RuntimeException e) {
                    log.error("SAVE: Unsupported input format: " + e.getClass() + " " + e.getMessage());
                    producerService.generateTextAnswer(update, "❗️Неверный формат ввода при сохранении материала!");
                }
            }
            case UPDATE -> {
                try {
                    processUpdateFromMassage(update, separator);
                } catch (RuntimeException e) {
                    log.error("UPDATE: Unsupported input format: " + e.getClass() + " " + e.getMessage());
                    producerService.generateTextAnswer(update, "❗️Неверный формат ввода при обновлении материала!");
                }
            }
            case DELETE -> {
                try {
                    processDeleteFromMassage(update, separator);
                } catch (RuntimeException e) {
                    log.error("DELETE: Unsupported input format: " + e.getClass() + " " + e.getMessage());
                    producerService.generateTextAnswer(update, "❗️Неверный формат ввода при удалении материала!");
                }
            }
            case null -> {
                log.info("Get materials case");
                val gotMaterials = materialService.getAllByName(text.toLowerCase());
                var answer = new StringBuilder();
                gotMaterials.forEach(material -> answer.append(describeMaterial(material)));
                if (answer.isEmpty()) {
                    producerService.generateTextAnswer(update, "Материалов с таким названием не найдено.");
                } else {
                    answer.deleteCharAt(answer.length() - 1);
                    producerService.generateTextAnswer(update, answer.toString());
                }
            }
        }
    }


    @Override
    public void processPrometheusRequest(PrometheusRequestData prometheusRequestData) {
        //TODO реализация
        producerService.generateTextAnswer(prometheusRequestData.getUpdate(), prometheusRequestData.toString());
    }

    @Override
    public void processSaveMaterialFromQueue(Material material) {
        material.setLastUpdate(LocalDateTime.now());
        var optional = materialService.getOptionalByNameAndSpecific(material.getName(),
                material.getSpecific());
        if (optional.isEmpty()) {
            materialService.save(material);
        } else {
            materialService.update(optional.get().getId(), material);
        }

    }

    private void processSaveFromMassage(Update update, char separator) {
        String[] textAndPrefix = update.getMessage().getText().split(Character.toString(separator), 2);
        String[] content = textAndPrefix[1].split(";", 4);
        log.info("Content to save: {}, {}, {}, {}", content[0], content[1], content[2], content[3]);
        val newMaterial = createMaterial(content[2], content[3],
                Double.parseDouble(content[0]), content[1]);
        materialService.save(newMaterial);
        producerService.generateTextAnswer(update, "Материал сохранен");

    }

    private void processUpdateFromMassage(Update update, char separator) {
        String[] textAndPrefix = update.getMessage().getText().split(Character.toString(separator), 2);
        String[] content = textAndPrefix[1].split(";", 5);
        log.info("ID to update: {}, Content to save: {}, {}, {}, {}", content[0], content[1], content[2],
                content[3], content[4]);
        val newMaterial = createMaterial(content[3], content[4],
                Double.parseDouble(content[1]), content[2]);
        materialService.update(Long.parseLong(content[0]), newMaterial);
        producerService.generateTextAnswer(update, "Материал обновлен");
    }

    private void processDeleteFromMassage(Update update, char separator) {
        String[] textAndPrefix = update.getMessage().getText().split(Character.toString(separator), 2);
        log.info("ID to del: {}", textAndPrefix[1]);
        materialService.delete(Long.parseLong(textAndPrefix[1]));
        producerService.generateTextAnswer(update, "Материал под номером "
                + Long.parseLong(textAndPrefix[1]) + " успешно удален");
    }

    private Material createMaterial(String name, String specific, Double cost, String packaging) {
        return Material.builder()
                .name(name.toLowerCase())
                .specific(specific)
                .cost(new BigDecimal(cost))
                .packaging(packaging)
                .lastUpdate(LocalDateTime.now())
                .build();
    }

    private String describeMaterial(Material material) {
        return "ID №: " + material.getId() + "\n" +
                "Название:  " + material.getName() + "\n Детали: " + material.getSpecific() + "\n" +
                "Цена за 1 " + material.getPackaging() + " " + material.getCost() + " $\n";
    }
}

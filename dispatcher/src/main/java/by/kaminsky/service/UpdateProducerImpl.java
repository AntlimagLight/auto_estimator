package by.kaminsky.service;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;

@Service
@Slf4j
@NoArgsConstructor
public class UpdateProducerImpl implements UpdateProducer {

    @Override
    public void produce(String kafkaQueue, Update update) {
        log.info(kafkaQueue + " : " + update.getMessage().getText());
    }
}

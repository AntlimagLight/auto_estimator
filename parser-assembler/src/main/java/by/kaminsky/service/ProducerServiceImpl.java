package by.kaminsky.service;

import by.kaminsky.dto.MaterialDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import static by.kaminsky.constants.RabbitQueue.NEW_MATERIALS;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProducerServiceImpl implements ProducerService {
    private final RabbitTemplate rabbitTemplate;

    @Override
    public void producerAnswer(MaterialDto materialDto) {
        rabbitTemplate.convertAndSend(NEW_MATERIALS, materialDto);
        log.info("send " + materialDto);
    }

}

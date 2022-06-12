package com.revoltcode.account.cmd.infrastructure.producer;

import com.revoltcode.cqrs.core.event.BaseEvent;
import com.revoltcode.cqrs.core.infrastructure.producer.EventProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class AccountEventProducer implements EventProducer {

    @Autowired
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    public void produce(String topic, BaseEvent event) {
        this.kafkaTemplate.send(topic, event);
    }
}

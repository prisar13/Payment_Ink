package com.service;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.model.dto.TransactionCreatedEvent;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class TransactionEventProducer {

    private final KafkaTemplate<String, TransactionCreatedEvent> kafkaTemplate;

    public TransactionEventProducer(KafkaTemplate<String, TransactionCreatedEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishTransactionCreatedEvent(String userId, TransactionCreatedEvent event) {
        kafkaTemplate.send("transaction.created", userId, event).whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("Sent event to partition={}, offset={}", result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            } else {
                log.error("Failed to send event", ex);
            }
        });
    }
}

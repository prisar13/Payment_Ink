package com.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.model.dto.FraudDecisionEvent;

@Service
public class FraudDecisionProducer {
    @Autowired
    private KafkaTemplate<String, FraudDecisionEvent> kafkaTemplate;

    public void sendFraudDecision(FraudDecisionEvent fraudDecisionEvent) {
        kafkaTemplate.send("fraud.decisions", fraudDecisionEvent.getTransactionId(), fraudDecisionEvent);
    }
}

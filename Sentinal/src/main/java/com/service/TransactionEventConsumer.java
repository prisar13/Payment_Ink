package com.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

import com.model.dto.ResponseDTO;
import com.model.dto.TransactionCreatedEvent;
import com.model.dto.TransactionRequest;

@Service
public class TransactionEventConsumer {

    @Autowired
    FraudService fraudEvaluationService;

    @KafkaListener(topics = "transaction.created", groupId = "fraud-group")
    public void consumeTransactionCreatedEvent(@Header(KafkaHeaders.RECEIVED_KEY) String userId,
            TransactionCreatedEvent event) {
        System.out.println("Received event for user: " + userId);
        System.out.println("Transaction ID: " + event.getTransactionId());
        TransactionRequest request = TransactionRequest.builder()
                .transactionId(event.getTransactionId())
                .userId(userId)
                .amount(event.getAmount())
                .ipAddress(event.getIpAddress())
                .build();
        ResponseDTO response = fraudEvaluationService.evaluateTransaction(request);
        System.out.println("Fraud decision: " + response.getDecision());
    }
}

package com.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

import com.model.constants.FraudDecision;
import com.model.dto.FraudDecisionEvent;
import com.model.dto.StatusUpdateDTO;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class FraudDecisionConsumer {
    @Autowired
    TransactionService transactionService;

    @KafkaListener(topics = "fraud.decisions", groupId = "status-group")
    public void getFraudDecision(@Header(KafkaHeaders.RECEIVED_KEY) String transactionId,
            FraudDecisionEvent fraudDecisionEvent) {
        try {
            StatusUpdateDTO statusUpdateDTO = new StatusUpdateDTO();
            statusUpdateDTO.setDecision(FraudDecision.valueOf(fraudDecisionEvent.getDecision()));
            statusUpdateDTO.setTransactionId(transactionId);

            transactionService.updateTransactionStatus(statusUpdateDTO);

        } catch (Exception e) {
            log.error("Failed processing fraud decision for {}", transactionId, e);
            throw e;
        }
    }
}

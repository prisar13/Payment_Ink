package com.service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.config.CallbackRetryQueue;
import com.model.constants.FraudDecision;
import com.model.dto.FraudQueueRequest;
import com.model.dto.FraudResult;
import com.model.dto.ResponseDTO;
import com.model.dto.TransactionRequest;
import com.model.entity.FraudEvaluation;
import com.repository.FraudEvaluationRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class FraudService {
    @Autowired
    private FraudEvaluationRepository fraudEvaluationRepository;
    @Autowired
    private CallbackRetryQueue callbackRetryQueue;
    @Autowired
    private StringRedisTemplate redisTemplate;

    public ResponseDTO evaluateTransaction(TransactionRequest request) {
        log.info("Evaluating transaction with ID: {}", request.getTransactionId());
        FraudEvaluation eval = setFraudEvaluationDetails(request);
        FraudResult result = evaluateRules(request);
        eval.setDecision(result.getDecision());
        eval.setRiskScore(result.getRiskScore());
        fraudEvaluationRepository.save(eval);
        callbackRetryQueue.sendCallback(new FraudQueueRequest(request.getTransactionId(), result.getDecision()));
        return new ResponseDTO(request.getTransactionId(), result.getDecision().name(), result.getReason());
    }

    private FraudEvaluation setFraudEvaluationDetails(TransactionRequest request) {
        FraudEvaluation fraudEvaluation = new FraudEvaluation();
        fraudEvaluation.setTransactionId(request.getTransactionId());
        fraudEvaluation.setAmount(request.getAmount());
        fraudEvaluation.setIpAddress(request.getIpAddress());
        fraudEvaluation.setEvaluatedAt(LocalDateTime.now());
        return fraudEvaluation;
    }

    private FraudResult evaluateRules(TransactionRequest request) {
        int riskScore = 0;
        List<String> reasons = new ArrayList<>();
        if (request.getAmount().compareTo(new BigDecimal("1000")) > 0) {
            riskScore += 40;
            reasons.add("High amount");
        }

        boolean userFraud = isVelocityFraud("user", request.getUserId(), 5);
        boolean ipFraud = isVelocityFraud("ip", request.getIpAddress(), 10);
        if (userFraud) {
            riskScore += 30;
            reasons.add("User velocity");
        }
        if (ipFraud) {
            riskScore += 20;
            reasons.add("IP velocity");
        }

        if (riskScore >= 60) {
            return new FraudResult(FraudDecision.BLOCKED, riskScore, reasons);
        } else if (riskScore >= 40) {
            return new FraudResult(FraudDecision.REVIEW, riskScore, reasons);
        }
        return new FraudResult(FraudDecision.APPROVED, riskScore, reasons);
    }

    private boolean isVelocityFraud(String keyPrefix, String value, int threshold) {
        String key = keyPrefix + ":" + value;
        Long count = redisTemplate.opsForValue().increment(key);
        if (count == 1) {
            redisTemplate.expire(key, Duration.ofMinutes(1));
        }

        return count > threshold;
    }
}

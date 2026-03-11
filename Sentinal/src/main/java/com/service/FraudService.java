package com.service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.model.constants.FraudDecision;
import com.model.dto.FraudAlertDTO;
import com.model.dto.FraudDecisionEvent;
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
    private FraudDecisionProducer fraudDecisionProducer;
    @Autowired
    private StringRedisTemplate redisTemplate;

    public ResponseDTO evaluateTransaction(TransactionRequest request) {
        log.info("Evaluating transaction with ID: {}", request.getTransactionId());
        FraudEvaluation eval = setFraudEvaluationDetails(request);
        FraudResult result = evaluateRules(request);
        eval.setDecision(result.getDecision());
        eval.setRiskScore(result.getRiskScore());
        fraudEvaluationRepository.save(eval);
        fraudDecisionProducer.sendFraudDecision(
                new FraudDecisionEvent(
                        request.getTransactionId(),
                        result.getDecision().toString(),
                        result.getRiskScore(),
                        result.getReasons()));
        return new ResponseDTO(request.getTransactionId(), result.getDecision().name(), result.getReasons().toString());
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

        // boolean userFraud = isVelocityFraud("user", request.getUserId(), 5);
        boolean ipFraud = isVelocityFraud("ip", request.getIpAddress(), 10);
        // if (userFraud) {
        // riskScore += 30;
        // reasons.add("User velocity");
        // }
        if (ipFraud) {
            riskScore += 20;
            reasons.add("IP velocity");
        }

        List<String> blacklistedCountries = List.of("CountryA", "CountryB");
        if (blacklistedCountries.contains(request.getCountry())) {
            riskScore += 30;
            reasons.add("Blacklisted country");
        }
        if (riskScore >= 70) {
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

    public Page<FraudAlertDTO> getFraudAlerts(int page, int size) {

        Page<FraudEvaluation> evaluationPage = fraudEvaluationRepository.findByDecisionNot(
                FraudDecision.APPROVED,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "evaluatedAt")));

        return evaluationPage.map(f -> FraudAlertDTO.builder()
                .transactionId(f.getTransactionId())
                .decision(f.getDecision())
                .riskScore(f.getRiskScore())
                .evaluatedAt(f.getEvaluatedAt())
                .build());
    }

}

package com.model.dto;

import java.time.LocalDateTime;

import com.model.constants.FraudDecision;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FraudAlertDTO {
    private String transactionId;
    private FraudDecision decision;
    private Double riskScore;
    private LocalDateTime evaluatedAt;
}

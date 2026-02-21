package com.model.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import com.model.constants.FraudDecision;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Data
@EqualsAndHashCode(callSuper = false)
@Table(name = "fraud_evaluations")
public class FraudEvaluation extends GenericTableConstants {
    private String transactionId;
    private BigDecimal amount;
    private double riskScore;
    @Enumerated(EnumType.STRING)
    private FraudDecision decision;
    private String triggeredRules;
    private LocalDateTime evaluatedAt;
    private String ipAddress;

}

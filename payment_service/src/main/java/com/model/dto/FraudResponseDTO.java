package com.model.dto;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class FraudResponseDTO {
    private String transactionId;
    private String decision;
    private BigDecimal riskScore;
    private String message;
}

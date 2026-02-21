package com.model.dto;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class FraudRequestDTO {
    private String transactionId;
    private BigDecimal amount;
    private String userId;
    private String country;
    private String ipAddress;
}

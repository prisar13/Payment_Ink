package com.model.dto;

import java.math.BigDecimal;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TransactionRequest {
    private String transactionId;
    private BigDecimal amount;
    private String userId;
    private String country;
    private String ipAddress;
}

package com.model.dto;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class TransactionCreatedEvent {
    private String transactionId;
    private String userId;
    private BigDecimal amount;
}
package com.model.dto;

import java.math.BigDecimal;

import com.model.constants.TransactionType;

import lombok.Data;

@Data
public class TransactionRequestDTO {
    private TransactionType type;
    private BigDecimal amount;
    private String currency;
    private String description;    
}

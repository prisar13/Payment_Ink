package com.model.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import com.model.constants.TransactionType;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Data;

@Data
public class TransactionResponseDTO {
    private UUID id;
    private BigDecimal amount;
    private String type;
    private String status;
    private String utr;
    private LocalDateTime createdAt;
}

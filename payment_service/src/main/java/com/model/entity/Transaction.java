package com.model.entity;

import java.math.BigDecimal;

import com.model.constants.Status;
import com.model.constants.TransactionType;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Data
@Table(name = "transactions")
@EqualsAndHashCode(callSuper = true)
public class Transaction extends GenericTableConstants {
    @Enumerated(EnumType.STRING)
    private TransactionType type;
    private String utr;
    private BigDecimal amount;
    private String description;
    @Enumerated(EnumType.STRING)
    private Status status;

}

package com.model.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FraudDecisionEvent {
    private String transactionId;
    private String decision;
    private double riskScore;
    private List<String> reasons;
}
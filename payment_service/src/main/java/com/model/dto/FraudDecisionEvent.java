package com.model.dto;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FraudDecisionEvent {
    private String transactionId;
    private String decision;
    private int riskScore;
    private List<String> reasons;
}
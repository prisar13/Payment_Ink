package com.model.dto;

import com.model.constants.FraudDecision;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FraudResult {

    private FraudDecision decision;
    private double riskScore;
    private String reason;
}

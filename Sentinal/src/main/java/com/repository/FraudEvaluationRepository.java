package com.repository;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.model.entity.FraudEvaluation;

public interface FraudEvaluationRepository extends JpaRepository<FraudEvaluation, UUID> {
    List<FraudEvaluation> findByTransactionId(String transactionId);
}

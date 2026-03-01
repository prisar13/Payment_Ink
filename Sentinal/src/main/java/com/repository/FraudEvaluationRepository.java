package com.repository;

import java.awt.print.Pageable;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;

import com.model.constants.FraudDecision;
import com.model.entity.FraudEvaluation;

public interface FraudEvaluationRepository extends JpaRepository<FraudEvaluation, UUID> {
    List<FraudEvaluation> findByTransactionId(String transactionId);

    Page<FraudEvaluation> findByDecision(FraudDecision decision, Pageable pageable);
}

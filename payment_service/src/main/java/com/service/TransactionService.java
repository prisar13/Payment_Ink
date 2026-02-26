package com.service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.util.Pair;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.model.constants.FraudDecision;
import com.model.constants.ResponseStatus;
import com.model.constants.Status;
import com.model.dto.FraudRequestDTO;
import com.model.dto.FraudResponseDTO;
import com.model.dto.ResponseDTO;
import com.model.dto.StatusUpdateDTO;
import com.model.dto.TransactionRequestDTO;
import com.model.entity.Transaction;
import com.repo.TransactionRepository;
import com.util.IdGeneratorUtil;
import com.util.JwtUtil;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class TransactionService {

    @Autowired
    private final JwtUtil jwtUtil;
    @Autowired
    private TransactionRepository transactionRepository;
    @Autowired
    private RestTemplate restTemplate;
    @Value("${fraud.service.url}")
    private String fraudServiceUrl;

    TransactionService(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    public ResponseDTO processTransaction(TransactionRequestDTO requestDTO) {
        Transaction transaction = new Transaction();
        transaction.setAmount(requestDTO.getAmount());
        transaction.setType(requestDTO.getType());
        transaction.setUtr(IdGeneratorUtil.generateUTR());
        transaction.setDescription(requestDTO.getDescription());
        transaction.setStatus(Status.PENDING);
        transactionRepository.save(transaction);

        FraudRequestDTO fraudRequest = new FraudRequestDTO();
        fraudRequest.setTransactionId(transaction.getId().toString());
        fraudRequest.setAmount(transaction.getAmount());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(jwtUtil.getServiceToken());
        HttpEntity<FraudRequestDTO> entity = new HttpEntity<>(fraudRequest, headers);

        CompletableFuture.runAsync(() -> {
            try {
                restTemplate.postForObject(fraudServiceUrl + "/evaluate", entity, FraudResponseDTO.class);
            } catch (Exception e) {
                // Note: need to handle retry logic for failed calls to fraud service, can use a
                // message queue or retry mechanism
                log.error("Failed to call fraud service for txnId {}: {}", transaction.getId(), e.getMessage());
            }
        });
        return new ResponseDTO(ResponseStatus.SUCCESS,
                "Transaction processed successfully for UTR: " + transaction.getUtr(), transaction.getId().toString(),
                null);
    }

    public String updateTransactionStatus(StatusUpdateDTO requestDTO) {
        Transaction transaction = transactionRepository.findById(UUID.fromString(requestDTO.getTransactionId()))
                .orElseThrow(
                        () -> new RuntimeException("Transaction not found with ID: " + requestDTO.getTransactionId()));

        Status mappedStatus = applyFraudDecision(requestDTO.getDecision(), transaction.getStatus());
        transaction.setStatus(mappedStatus);
        transactionRepository.save(transaction);
        return "Transaction status updated successfully for UTR: " + transaction.getUtr();
    }

    private Status applyFraudDecision(FraudDecision status, Status currentStatus) {
        Map<Pair<Status, FraudDecision>, Status> transitionMap = Map.of(
                Pair.of(Status.PENDING, FraudDecision.BLOCKED), Status.FAILED,
                Pair.of(Status.PENDING, FraudDecision.REVIEW), Status.REVIEW,
                Pair.of(Status.PENDING, FraudDecision.APPROVED), Status.COMPLETED);
        Status mappedStatus = transitionMap.getOrDefault(Pair.of(currentStatus, status), currentStatus);
        if (!isAllowedTransition(currentStatus, mappedStatus)) {
            return currentStatus;
        }
        return mappedStatus;
    }

    private boolean isAllowedTransition(Status current, Status next) {
        if (current == next)
            return false;

        if (current == Status.REVIEW)
            return false;

        return true;
    }
}

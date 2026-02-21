package com.service;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

@Service
public class TransactionService {
    @Autowired
    private TransactionRepository transactionRepository;
    @Autowired
    private RestTemplate restTemplate;
    @Value("${fraud.service.url}")
    private String fraudServiceUrl;

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

        CompletableFuture.runAsync(() -> {
            restTemplate.postForObject(
                    fraudServiceUrl + "/evaluate",
                    fraudRequest,
                    FraudResponseDTO.class);
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
        Status mappedStatus = currentStatus;
        switch (status) {
            case BLOCKED:
                if (currentStatus == Status.PENDING) {
                    mappedStatus = Status.FAILED;
                } else if (currentStatus == Status.COMPLETED) {
                    mappedStatus = Status.REVIEW;
                }
                break;
            case APPROVED:
                if (currentStatus == Status.PENDING)
                    mappedStatus = Status.COMPLETED;
                break;
            default:
                return Status.PENDING;
        }
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

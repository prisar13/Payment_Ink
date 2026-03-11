package com.service;

import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.util.Pair;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.model.constants.FraudDecision;
import com.model.constants.ResponseStatus;
import com.model.constants.Status;
import com.model.dto.FraudRequestDTO;
import com.model.dto.ResponseDTO;
import com.model.dto.StatusUpdateDTO;
import com.model.dto.TransactionCreatedEvent;
import com.model.dto.TransactionRequestDTO;
import com.model.dto.TransactionResponseDTO;
import com.model.entity.Transaction;
import com.repo.TransactionRepository;
import com.util.IdGeneratorUtil;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class TransactionService {

    @Autowired
    private TransactionRepository transactionRepository;
    @Autowired
    private KafkaTemplate<String, TransactionCreatedEvent> kafkaTemplate;
    @Value("${fraud.service.url}")
    private String fraudServiceUrl;

    private static final Map<Pair<Status, FraudDecision>, Status> TRANSITION_MAP = Map.of(
            Pair.of(Status.PENDING, FraudDecision.BLOCKED), Status.FAILED,
            Pair.of(Status.PENDING, FraudDecision.REVIEW), Status.REVIEW,
            Pair.of(Status.PENDING, FraudDecision.APPROVED), Status.COMPLETED);

    public ResponseDTO processTransaction(TransactionRequestDTO requestDTO, String ipaddressString) {
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
        // fraudRequest.setUserId();
        fraudRequest.setIpAddress(ipaddressString);
        fraudRequest.setCountry("India"); // Note: can use geoip service to get country from IP
        TransactionEventProducer producer = new TransactionEventProducer(kafkaTemplate);
        TransactionCreatedEvent event = new TransactionCreatedEvent();
        event.setTransactionId(transaction.getId().toString());
        event.setUserId("userId1");
        event.setAmount(transaction.getAmount());
        event.setIpAddress(ipaddressString);
        producer.publishTransactionCreatedEvent("userId1", event);
        return new ResponseDTO(ResponseStatus.SUCCESS,
                "Transaction processed successfully for UTR: " + transaction.getUtr(), transaction.getId().toString(),
                null);
    }

    @Transactional
    public String updateTransactionStatus(StatusUpdateDTO requestDTO) {
        Transaction transaction = transactionRepository.findById(UUID.fromString(requestDTO.getTransactionId()))
                .orElseThrow(
                        () -> new RuntimeException("Transaction not found with ID: " + requestDTO.getTransactionId()));
        if (transaction.getStatus() != Status.PENDING) {
            return "Transaction already processed";
        }
        Status mappedStatus = applyFraudDecision(requestDTO.getDecision(), transaction.getStatus());
        transaction.setStatus(mappedStatus);
        transactionRepository.save(transaction);
        return "Transaction status updated successfully for UTR: " + transaction.getUtr();
    }

    private Status applyFraudDecision(FraudDecision status, Status currentStatus) {
        Status mappedStatus = TRANSITION_MAP.getOrDefault(Pair.of(currentStatus, status), currentStatus);
        if (!isAllowedTransition(currentStatus, mappedStatus)) {
            return currentStatus;
        }
        return mappedStatus;
    }

    private boolean isAllowedTransition(Status current, Status next) {
        if (current == Status.COMPLETED || current == Status.FAILED || current == Status.REVIEW) {
            return false;
        }
        return current != next;
    }

    public Page<TransactionResponseDTO> getTransactions(int page, int size) {
        return transactionRepository.findAll(PageRequest.of(page, size, Sort.by("createdAt").descending()))
                .map(this::mapToResponseDTO);
    }

    private TransactionResponseDTO mapToResponseDTO(Transaction transaction) {
        TransactionResponseDTO responseDTO = new TransactionResponseDTO();
        responseDTO.setId(transaction.getId());
        responseDTO.setAmount(transaction.getAmount());
        responseDTO.setType(transaction.getType().name());
        responseDTO.setStatus(transaction.getStatus().name());
        responseDTO.setUtr(transaction.getUtr());
        responseDTO.setCreatedAt(transaction.getCreatedAt());
        return responseDTO;
    }
}

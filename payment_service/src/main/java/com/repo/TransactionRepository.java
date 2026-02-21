package com.repo;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.model.entity.Transaction;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
    
}

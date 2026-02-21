package com.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.model.dto.ResponseDTO;
import com.model.dto.StatusUpdateDTO;
import com.model.dto.TransactionRequestDTO;
import com.service.TransactionService;

@EnableMethodSecurity
@RestController
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    @PreAuthorize("hasRole('ADMIN', 'USER')")
    @PostMapping(value = "/create")
    public ResponseDTO createTransaction(@RequestBody TransactionRequestDTO requestDTO) {
        return transactionService.processTransaction(requestDTO);
    }

    @PreAuthorize("hasRole('ADMIN', 'USER')")
    @PostMapping(value = "/statusUpdate")
    public String updateTransactionStatus(@RequestBody StatusUpdateDTO requestDTO) {
        return transactionService.updateTransactionStatus(requestDTO);
    }

}

package com.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.model.dto.ResponseDTO;
import com.model.dto.StatusUpdateDTO;
import com.model.dto.TransactionRequestDTO;
import com.model.dto.TransactionResponseDTO;
import com.service.TransactionService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/transaction")
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    @PreAuthorize("hasAnyRole('ADMIN', 'DEVELOPER', 'TESTER', 'PRODUCT_OWNER')")
    @PostMapping(value = "/create")
    public ResponseDTO createTransaction(@RequestBody TransactionRequestDTO requestDTO,
            HttpServletRequest httpRequest) {
        String ip = getClientIp(httpRequest);
        // Long userId = extractUserIdFromJwt(httpRequest);
        return transactionService.processTransaction(requestDTO, ip);
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

    @PreAuthorize("hasAnyRole('ADMIN','SERVICE')")
    @PostMapping(value = "/statusUpdate")
    public String updateTransactionStatus(@RequestBody StatusUpdateDTO requestDTO) {
        return transactionService.updateTransactionStatus(requestDTO);
    }

    @PreAuthorize("hasAnyRole('ADMIN','DEVELOPER')")
    @GetMapping
    public Page<TransactionResponseDTO> getTransactions(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return transactionService.getTransactions(page, size);
    }

}

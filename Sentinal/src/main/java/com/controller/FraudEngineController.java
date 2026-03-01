package com.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.model.dto.FraudAlertDTO;
import com.model.dto.PageResponseDTO;
import com.model.dto.ResponseDTO;
import com.model.dto.TransactionRequest;
import com.service.FraudService;

@RestController
public class FraudEngineController {

    @Autowired
    FraudService fraudService;

    @PostMapping(value = "/evaluate", consumes = "application/json", produces = "application/json")
    public ResponseDTO evaluateTransaction(@RequestBody TransactionRequest request) {
        return fraudService.evaluateTransaction(request);
    }

    @GetMapping("/alerts")
    public PageResponseDTO<FraudAlertDTO> getFraudAlerts(@RequestParam int page, @RequestParam int size) {
        Page<FraudAlertDTO> alerts = fraudService.getFraudAlerts(page, size);
        PageResponseDTO<FraudAlertDTO> response = new PageResponseDTO<>();
        response.setContent(alerts.getContent());
        response.setTotalPages(alerts.getTotalPages());
        response.setTotalElements(alerts.getTotalElements());
        response.setNumber(alerts.getNumber());
        response.setSize(alerts.getSize());
        return response;
    }
}

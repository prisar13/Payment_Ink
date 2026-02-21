package com.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

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
}

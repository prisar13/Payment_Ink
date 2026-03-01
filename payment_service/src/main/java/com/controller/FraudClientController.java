package com.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.model.constants.ResponseStatus;
import com.model.dto.FraudAlertDTO;
import com.model.dto.ResponseDTO;
import com.service.FraudClient;

@RestController
@RequestMapping("/fraud")
public class FraudClientController {

    @Autowired
    private FraudClient fraudClient;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/alerts")
    public ResponseDTO getFraudAlerts(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return new ResponseDTO(
                ResponseStatus.SUCCESS,
                "Alerts fetched",
                fraudClient.fetchAlerts(page, size),
                null);
    }
}

package com.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.model.dto.LoginRequest;
import com.model.dto.ResponseDTO;
import com.service.AuthService;

@RestController
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/auth/register")
    public ResponseEntity<ResponseDTO> register(@RequestBody LoginRequest req) {
        return authService.register(req);
    }

    @PostMapping("/auth/login")
    public ResponseEntity<ResponseDTO> login(@RequestBody LoginRequest req) {
        return authService.login(req);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/auth/assign-role")
    public ResponseEntity<ResponseDTO> assignUserRole(@RequestBody LoginRequest req) {
        return authService.assignUserRole(req);
    }
}

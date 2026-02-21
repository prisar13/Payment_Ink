package com.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.model.dto.AppUser;
import com.model.constants.ResponseStatus;
import com.model.constants.Role;
import com.model.dto.LoginRequest;
import com.model.dto.ResponseDTO;
import com.repo.UserRepository;
import com.util.JwtUtil;

@Service
public class AuthService {

    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private UserRepository userRepository;

    public ResponseDTO register(LoginRequest req) {
        AppUser user = new AppUser();
        user.setUsername(req.getUsername());
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setRole(req.getRole() != null ? Role.valueOf(req.getRole().toUpperCase()) : Role.USER);
        if (userRepository.findByUsername(req.getUsername()).isPresent()) {
            return new ResponseDTO(ResponseStatus.FAILED, "Username already exists", null, null);
        }
        userRepository.save(user);
        return new ResponseDTO(ResponseStatus.SUCCESS, "User registered successfully", null, null);
    }

    public ResponseDTO login(LoginRequest req) {
        Optional<AppUser> user = userRepository.findByUsername(req.getUsername());
        if (user.isEmpty()) {
            return new ResponseDTO(ResponseStatus.FAILED, "User not found", null, null);
        }
        if (!passwordEncoder.matches(req.getPassword(), user.get().getPassword())) {
            return new ResponseDTO(ResponseStatus.FAILED, "Invalid credentials", null, null);
        }
        String token = jwtUtil.generateToken(user.get().getUsername());
        return new ResponseDTO(ResponseStatus.SUCCESS, "Login successful", null, token);
    }
}

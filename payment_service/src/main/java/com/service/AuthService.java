package com.service;

import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.model.constants.ResponseStatus;
import com.model.constants.RoleType;
import com.model.dto.LoginRequest;
import com.model.dto.ResponseDTO;
import com.model.entity.AppUser;
import com.repo.RoleRepository;
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
    @Autowired
    private RoleRepository roleRepository;

    public ResponseEntity<ResponseDTO> register(LoginRequest req) {
        AppUser user = new AppUser();
        user.setUsername(req.getUsername());
        user.setPassword(passwordEncoder.encode(req.getPassword()));

        user.setRoles(Set.of(roleRepository.findByName(RoleType.DEVELOPER)
                .orElseThrow(() -> new RuntimeException("Role not found"))));
        if (userRepository.findByUsername(req.getUsername()).isPresent()) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(new ResponseDTO(ResponseStatus.FAILED, "Username already exists", null, null));
        }
        userRepository.save(user);
        return ResponseEntity.ok(new ResponseDTO(ResponseStatus.SUCCESS, "User registered successfully", null, null));
    }

    public ResponseEntity<ResponseDTO> login(LoginRequest req) {

        Optional<AppUser> user = userRepository.findByUsername(req.getUsername());

        if (user.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(new ResponseDTO(ResponseStatus.FAILED, "User not found", null, null));
        }

        if (!passwordEncoder.matches(req.getPassword(), user.get().getPassword())) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(new ResponseDTO(ResponseStatus.FAILED, "Invalid credentials", null, null));
        }

        String token = jwtUtil.generateToken(user.get().getUsername());

        return ResponseEntity.ok(
                new ResponseDTO(ResponseStatus.SUCCESS, "Login successful", null, token));
    }

    public ResponseEntity<ResponseDTO> assignUserRole(LoginRequest req) {
        AppUser user = new AppUser();
        user.setUsername(req.getUsername());
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setRoles(Set.of(roleRepository.findByName(RoleType.valueOf(req.getRole().toUpperCase()))
                .orElseThrow(() -> new RuntimeException("Role not found"))));
        if (userRepository.findByUsername(req.getUsername()).isPresent()) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(new ResponseDTO(ResponseStatus.FAILED, "Username already exists", null, null));
        }
        userRepository.save(user);
        return ResponseEntity.ok(new ResponseDTO(ResponseStatus.SUCCESS, "Admin registered successfully", null, null));
    }
}

package com.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.model.dto.FraudAlertDTO;
import com.model.dto.PageResponseDTO;
import com.util.JwtUtil;

@Service
public class FraudClient {
    @Value("${fraud.service.url}")
    private String fraudServiceUrl;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private JwtUtil jwtUtil;

    public PageResponseDTO<FraudAlertDTO> fetchAlerts(int page, int size) {

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(jwtUtil.getServiceToken());

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        String url = fraudServiceUrl + "/alerts?page=" + page + "&size=" + size;

        ResponseEntity<PageResponseDTO<FraudAlertDTO>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<PageResponseDTO<FraudAlertDTO>>() {
                });

        return response.getBody();
    }
}

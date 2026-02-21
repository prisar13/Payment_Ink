package com.model.dto;

import lombok.Data;

@Data
public class RetryTask {
    private FraudQueueRequest request;
    private int retryCount;
    private Long nextRetryTime;
}

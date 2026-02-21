package com.config;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.model.dto.FraudQueueRequest;
import com.model.dto.RetryTask;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class CallbackRetryQueue {
    @Autowired
    private RestTemplate restTemplate;
    @Value("${transaction.service.url}")
    private String transactionServiceUrl;

    private final Queue<RetryTask> retryQueue = new ConcurrentLinkedQueue<>();

    public void add(FraudQueueRequest request) {
        RetryTask task = new RetryTask();
        task.setRequest(request);
        task.setRetryCount(0);
        task.setNextRetryTime(System.currentTimeMillis());
        retryQueue.offer(task);
    }

    public RetryTask poll() {
        return retryQueue.poll();
    }

    @Scheduled(fixedDelay = 5000)
    public void retryCallbacks() {
        log.info("Retry scheduler tick. Queue size={}", retryQueue.size());
        RetryTask task;
        while ((task = retryQueue.poll()) != null) {
            log.info("Retrying {}", task.getRequest().getTransactionId());
            if (System.currentTimeMillis() < task.getNextRetryTime()) {
                retryQueue.offer(task);
                break;
            }
            try {
                ResponseEntity<Void> response = restTemplate.postForEntity(
                        transactionServiceUrl + "/statusUpdate",
                        task.getRequest(),
                        Void.class);

                if (!response.getStatusCode().is2xxSuccessful()) {
                    throw new RestClientException("Callback failed with status "
                            + response.getStatusCode());
                }
            } catch (RestClientException e) {
                if (task.getRetryCount() >= 5) {
                    log.error("Max retries reached for {}", task.getRequest().getTransactionId());
                    continue;
                }

                task.setRetryCount(task.getRetryCount() + 1);
                long delay = calculateBackoff(task.getRetryCount());
                task.setNextRetryTime(System.currentTimeMillis() + delay);
                retryQueue.offer(task);
            }
        }
    }

    public void sendCallback(FraudQueueRequest request) {
        try {
            ResponseEntity<Void> response = restTemplate.postForEntity(
                    transactionServiceUrl + "/statusUpdate",
                    request,
                    Void.class);

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new RestClientException("Callback failed");
            }
        } catch (RestClientException e) {
            log.error("Callback failed, adding to retry queue");
            add(request);
        }
    }

    private long calculateBackoff(int retryCount) {
        return (long) Math.pow(2, retryCount) * 5000;
    }
}

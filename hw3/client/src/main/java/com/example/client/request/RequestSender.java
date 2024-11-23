package com.example.client.request;

import com.example.client.pojo.SkiersRequest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class RequestSender implements Runnable {

    private final BlockingQueue<SkiersRequest> requestQueue;
    private final RestTemplate restTemplate;
    private final AtomicInteger successCount;
    private final AtomicInteger failureCount;
    private final String apiUrl;
    private final int maxRetries = 5;

    // The queue used to record the response time
    private final ConcurrentLinkedQueue<Long> responseTimes;

    // Queue to hold log data (for CSV)
    private final ConcurrentLinkedQueue<String[]> logRecords;

    public RequestSender(BlockingQueue<SkiersRequest> requestQueue, RestTemplate restTemplate,
                         AtomicInteger successCount, AtomicInteger failureCount,
                         String apiUrl, ConcurrentLinkedQueue<Long> responseTimes,
                         ConcurrentLinkedQueue<String[]> logRecords) {
        this.requestQueue = requestQueue;
        this.restTemplate = restTemplate;
        this.successCount = successCount;
        this.failureCount = failureCount;
        this.apiUrl = apiUrl;
        this.responseTimes = responseTimes;
        this.logRecords = logRecords;
    }

    @Override
    public void run() {
        while (!requestQueue.isEmpty()) {
            try {
                // Retrieve the request data from the queue
                SkiersRequest request = requestQueue.take();
                int retries = 0;
                boolean success = false;

                // Set the HTTP header and specify Content-Type as application/json
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                HttpEntity<SkiersRequest> entity = new HttpEntity<>(request, headers);

                while (retries < maxRetries && !success) {
                    long startTime = System.currentTimeMillis();
                    try {
                        ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, entity, String.class);
                        long endTime = System.currentTimeMillis();
                        long delay = endTime - startTime;

                        responseTimes.add(delay);

                        logRecords.add(new String[]{
                                String.valueOf(startTime),
                                "POST",
                                String.valueOf(delay),
                                String.valueOf(response.getStatusCodeValue())
                        });

                        if (response.getStatusCode().is2xxSuccessful()) {
                            successCount.incrementAndGet();
                            success = true;
                        } else {
                            retries++;
                        }
                    } catch (Exception e) {
                        retries++;
                        System.err.println("Request failed on attempt " + retries + " for request: " + request);
                        e.printStackTrace();
                    }
                }

                if (!success) {
                    failureCount.incrementAndGet();
                }

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
}

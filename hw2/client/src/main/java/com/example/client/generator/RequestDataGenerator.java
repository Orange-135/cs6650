package com.example.client.generator;

import com.example.client.pojo.SkiersRequest;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadLocalRandom;

public class RequestDataGenerator implements Runnable {

    private final BlockingQueue<SkiersRequest> requestQueue;
    private final int totalRequests;

    public RequestDataGenerator(BlockingQueue<SkiersRequest> requestQueue, int totalRequests) {
        this.requestQueue = requestQueue;
        this.totalRequests = totalRequests;
    }

    @Override
    public void run() {
        for (int i = 0; i < totalRequests; i++) {
            SkiersRequest request = new SkiersRequest(
                    ThreadLocalRandom.current().nextInt(1, 100001),
                    ThreadLocalRandom.current().nextInt(1, 11),
                    ThreadLocalRandom.current().nextInt(1, 41),
                    2024,
                    1,
                    ThreadLocalRandom.current().nextInt(1, 361)
            );
            try {
                requestQueue.put(request);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
}

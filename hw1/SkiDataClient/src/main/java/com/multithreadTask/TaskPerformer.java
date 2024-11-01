package com.multithreadTask;
import com.model.SkierLiftRideEvent;

import java.net.http.HttpClient;
import java.util.concurrent.*;

public class TaskPerformer {
    public int eventQueueCapacity;
    public int numEvents;
    public int initialThreadPoolSize;
    public int maxThreadPoolSize;
    public RequestCounter counter = new RequestCounter();
    public CountDownLatch initialLatch;
    public CountDownLatch totalRequestsLatch;
    public HttpClient httpClient;
    public DynamicThreadPoolExecutor executor;
    public BlockingQueue<SkierLiftRideEvent> queue;

    public TaskPerformer(int eventQueueCapacity, int numEvents, int initialThreadPoolSize, int maxThreadPoolSize) {
        this.eventQueueCapacity = eventQueueCapacity;
        this.numEvents = numEvents;
        this.initialThreadPoolSize = initialThreadPoolSize;
        this.maxThreadPoolSize = maxThreadPoolSize;
        this.initialLatch = new CountDownLatch(1);
        this.totalRequestsLatch = new CountDownLatch(numEvents);
        this.httpClient = HttpClient.newHttpClient();
        this.queue = new LinkedBlockingQueue<>(eventQueueCapacity);

    }

    public void printResults(long startTime, long endTime) {
        double totalTime = (endTime - startTime) / 1000.0;
        double throughput = numEvents / totalTime;
        System.out.println("Event queue capacity : " + this.eventQueueCapacity);
        System.out.println("Max number of Threads : " + this.maxThreadPoolSize);
        System.out.println("Total run time: " + totalTime + " s");
        System.out.println("Total throughput: " + throughput + " requests/s");
        System.out.println("Total successful requests: " + counter.getSuccessfulRequests());
        System.out.println("Total failed requests: " + counter.getFailedRequests());
    }
}

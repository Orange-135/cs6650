package com.example.client;

import com.example.client.generator.RequestDataGenerator;
import com.example.client.pojo.SkiersRequest;
import com.example.client.request.RequestSender;
import com.opencsv.CSVWriter;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.client.RestTemplate;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@SpringBootApplication
public class ClientApplication implements CommandLineRunner {

    private final String apiUrl = "http://alb-1586008584.us-west-2.elb.amazonaws.com/skiers";
    private final int threadCount = 32;
    private final RestTemplate restTemplate = new RestTemplate();

    // The queue used to record all response times
    private final ConcurrentLinkedQueue<Long> responseTimes = new ConcurrentLinkedQueue<>();

    // Queue to hold log data (for CSV)
    private final ConcurrentLinkedQueue<String[]> logRecords = new ConcurrentLinkedQueue<>();

    public static void main(String[] args) {
        SpringApplication.run(ClientApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {

        // Run a simple test and send eg 10000 requests from a single thread to do this.
//        processRequests(10000, 1);



        // First batch: 32,000 requests
        System.out.println("Start the first batch (32,000 requests)");
        processRequests(1000, 32);

        // Second batch: 165,000 requests
        System.out.println("Start the second batch (165,000 requests)");
        processRequests(5160, 32);  // 165000 / 32 ≈ 5160



        // Save all request logs to CSV files
        saveLogsToCSV();
    }

    private void processRequests(int requestsPerThread, int threadCount) throws InterruptedException {
        BlockingQueue<SkiersRequest> requestQueue = new LinkedBlockingQueue<>();
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        long startWallTime = System.currentTimeMillis();

        // Start the request data generation thread
        Thread dataGenerator = new Thread(new RequestDataGenerator(requestQueue, requestsPerThread * threadCount));
        dataGenerator.start();

        // Start the request sending thread
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        for (int i = 0; i < threadCount; i++) {
            executor.submit(new RequestSender(requestQueue, restTemplate, successCount, failureCount, apiUrl, responseTimes, logRecords));
        }

        // Wait for the data generation to complete
        dataGenerator.join();

        // Wait for all request sending threads to complete
        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.HOURS);

        long endWallTime = System.currentTimeMillis();

        // Outputs statistics for the current batch
        printStatistics(startWallTime, endWallTime, successCount.get(), failureCount.get());
    }

    private void printStatistics(long startWallTime, long endWallTime, int totalSuccess, int totalFailures) {
        List<Long> sortedResponseTimes = new ArrayList<>(responseTimes);
        Collections.sort(sortedResponseTimes);

        long totalRequests = totalSuccess + totalFailures;
        double totalTimeSeconds = (endWallTime - startWallTime) / 1000.0;
        double throughput = totalRequests / totalTimeSeconds;

        double averageResponseTime = sortedResponseTimes.stream().mapToLong(Long::longValue).average().orElse(0);
        long medianResponseTime = sortedResponseTimes.get(sortedResponseTimes.size() / 2);
        long p99ResponseTime = sortedResponseTimes.get((int) (sortedResponseTimes.size() * 0.99) - 1);
        long minResponseTime = sortedResponseTimes.get(0);
        long maxResponseTime = sortedResponseTimes.get(sortedResponseTimes.size() - 1);

        System.out.println("Request statistics：");
        System.out.println("Number of successful requests: " + totalSuccess);
        System.out.println("Number of failed requests: " + totalFailures);
        System.out.println("Total running time (ms): " + (endWallTime - startWallTime));
        System.out.println("Average response time (ms): " + averageResponseTime);
        System.out.println("Median response time (ms): " + medianResponseTime);
        System.out.println("P99 Response time (ms): " + p99ResponseTime);
        System.out.println("Minimum response time (ms): " + minResponseTime);
        System.out.println("Maximum response time (ms): " + maxResponseTime);
        System.out.println("Requests per second Throughput (requests/second): " + throughput);
    }

    // A method to save all request logs as CSV files
    private void saveLogsToCSV() {
        try {
            Files.createDirectories(Paths.get("logs"));  // Creating the logs directory
            FileWriter outputfile = new FileWriter("logs/request_logs.csv");

            try (CSVWriter writer = new CSVWriter(outputfile)) {
                writer.writeNext(new String[]{"Start Time", "Request Type", "Delay (ms)", "Response Code"});
                writer.writeAll(logRecords);  // Write the request records of all threads
            }
            System.out.println("The request log was successfully saved to logs/request_logs.csv");

        } catch (IOException e) {
            System.err.println("Unable to save request logs to CSV files");
            e.printStackTrace();
        }
    }
}

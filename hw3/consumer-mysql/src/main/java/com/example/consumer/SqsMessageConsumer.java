package com.example.consumer;

import com.example.consumer.pojo.SkiersRequest;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class SqsMessageConsumer {

    private final SqsClient sqsClient;
    private final String queueUrl = "https://sqs.us-west-2.amazonaws.com/713021927523/sqs"; // SQS URL
    private final ExecutorService executorService;
    private final HikariDataSource dataSource;
    private final ObjectMapper objectMapper;

    @Autowired
    public SqsMessageConsumer(SqsClient sqsClient) {
        this.sqsClient = sqsClient;
        this.executorService = Executors.newFixedThreadPool(100);
        this.objectMapper = new ObjectMapper();

        // Initialize HikariCP DataSource
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://skiers-database-1.cluster-cgeiiiis0kyv.us-west-2.rds.amazonaws.com:3306/skiers"); // Update with your DB URL
        config.setUsername("root"); // Update with your DB username
        config.setPassword("3112520587"); // Update with your DB password
        config.setMaximumPoolSize(10); // Connection pool size
        this.dataSource = new HikariDataSource(config);
    }

    @Scheduled(fixedRate = 1000)
    public void pollMessages() {
        // Configure receive message request
        ReceiveMessageRequest receiveMessageRequest = ReceiveMessageRequest.builder()
                .queueUrl(queueUrl)
                .maxNumberOfMessages(10)
                .waitTimeSeconds(10)
                .build();

        List<Message> messages = sqsClient.receiveMessage(receiveMessageRequest).messages();

        for (Message message : messages) {
            executorService.submit(() -> processMessage(message));
        }
    }

    private void processMessage(Message message) {
        try {
            // Deserialize message body to SkiersRequest object
            SkiersRequest skiersRequest = objectMapper.readValue(message.body(), SkiersRequest.class);

            // Save data to MySQL
            saveToDatabase(skiersRequest);

            // Delete message after processing
            deleteMessage(message);
        } catch (Exception e) {
            System.err.println("Failed to process message: " + message.body());
            e.printStackTrace();
        }
    }

    private void saveToDatabase(SkiersRequest skiersRequest) {
        String sql = "INSERT INTO SkiersRequest (skierID, resortID, liftID, seasonID, dayID, time) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setInt(1, skiersRequest.getSkierID());
            preparedStatement.setInt(2, skiersRequest.getResortID());
            preparedStatement.setInt(3, skiersRequest.getLiftID());
            preparedStatement.setInt(4, skiersRequest.getSeasonID());
            preparedStatement.setInt(5, skiersRequest.getDayID());
            preparedStatement.setInt(6, skiersRequest.getTime());

            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Failed to insert data into the database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void deleteMessage(Message message) {
        DeleteMessageRequest deleteMessageRequest = DeleteMessageRequest.builder()
                .queueUrl(queueUrl)
                .receiptHandle(message.receiptHandle())
                .build();

        sqsClient.deleteMessage(deleteMessageRequest);
        System.out.println("Deleted message with ID: " + message.messageId());
    }
}

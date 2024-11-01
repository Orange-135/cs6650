package com.example.consumer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class SqsMessageConsumer {

    private final SqsClient sqsClient;
    private final String queueUrl = "https://sqs.us-west-2.amazonaws.com/713021927523/sqs"; // SQS URL
    private final ExecutorService executorService;

    @Autowired
    public SqsMessageConsumer(SqsClient sqsClient) {
        this.sqsClient = sqsClient;
        this.executorService = Executors.newFixedThreadPool(100);
    }


    @Scheduled(fixedRate = 1000)
    public void pollMessages() {
        // 配置接收消息请求
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
            // 处理消息的逻辑
            System.out.println("Processing message: " + message.body());

            // 处理完成后删除消息
            deleteMessage(message);
        } catch (Exception e) {
            System.err.println("Failed to process message: " + message.body());
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

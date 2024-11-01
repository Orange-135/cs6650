package com.example.server.controller;

import com.example.server.pojo.SkiersRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

@RestController
@RequestMapping("/skiers")
@Validated
public class SkiersController {

    private final SqsClient sqsClient;
    private final ObjectMapper objectMapper;
    private final String queueUrl = "https://sqs.us-west-2.amazonaws.com/713021927523/sqs"; // SQS URL

    @Autowired
    public SkiersController(SqsClient sqsClient, ObjectMapper objectMapper) {
        this.sqsClient = sqsClient;
        this.objectMapper = objectMapper;
    }

    @PostMapping
    public ResponseEntity<?> createSkiersEntry(@Valid @RequestBody SkiersRequest skiersRequest) {
        try {
            String messageBody = objectMapper.writeValueAsString(skiersRequest);

            SendMessageRequest sendMsgRequest = SendMessageRequest.builder()
                    .queueUrl(queueUrl)
                    .messageBody(messageBody)
                    .build();

            sqsClient.sendMessage(sendMsgRequest);

            return ResponseEntity.status(HttpStatus.CREATED).body(
                    String.format(
                            "Skiers data recorded and sent to SQS: skierID=%d, resortID=%d, liftID=%d, seasonID=%d, dayID=%d, time=%d",
                            skiersRequest.getSkierID(),
                            skiersRequest.getResortID(),
                            skiersRequest.getLiftID(),
                            skiersRequest.getSeasonID(),
                            skiersRequest.getDayID(),
                            skiersRequest.getTime()
                    )
            );
        } catch (JsonProcessingException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to process request");
        }
    }

    @GetMapping
    public String check() {
        return "Build Successful!";
    }
}

package com.ycompany.fraudservice.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ycompany.fraudservice.model.FraudDecision;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageResponse;

import java.util.Map;

@Service
@Slf4j
public class FraudWorker {

    private final SqsClient sqs;
    private final DynamoDbClient dynamo;
    private final FraudEngine engine;
    private final SurveyorAssignmentService assignmentService;


    @Value("${fraud.queueUrl}")
    private String queueUrl;

    @Value("${fraud.claimsTable}")
    private String table;

    public FraudWorker(SqsClient sqs,
                       DynamoDbClient dynamo,
                       FraudEngine engine,
                       SurveyorAssignmentService assignmentService) {
        this.sqs = sqs;
        this.dynamo = dynamo;
        this.engine = engine;
        this.assignmentService = assignmentService;
    }

    @Scheduled(fixedDelay = 3000)
    public void poll() {
        ReceiveMessageResponse response =
                sqs.receiveMessage(r -> r
                        .queueUrl(queueUrl)
                        .maxNumberOfMessages(5)
                        .waitTimeSeconds(10));

        for (Message msg : response.messages()) {
            process(msg);
        }
    }

    private void process(Message msg) {
        try {
            String body = msg.body();
            String claimId = extractClaimId(body);

            // 1️⃣ Assign surveyor immediately after claim submission
            assignmentService.assignSurveyor(claimId);

            // 2️⃣ Simulate fraud delay
            Thread.sleep(5000);

            // 3️⃣ Fraud evaluation (existing logic)
            FraudDecision decision = engine.evaluate(claimId);
            updateClaimStatus(claimId, decision);

            sqs.deleteMessage(r -> r
                    .queueUrl(queueUrl)
                    .receiptHandle(msg.receiptHandle()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void updateClaimStatus(String claimId, FraudDecision decision) {
        log.info("Processing claim: {}", claimId);
        String status =
                switch (decision) {
                    case LOW_RISK -> "FRAUD_CHECKED";
                    case MEDIUM_RISK -> "REVIEW_REQUIRED";
                    case HIGH_RISK -> "FRAUD_FLAGGED";
                };

        dynamo.updateItem(r -> r
                .tableName(table)
                .key(Map.of("claimId", AttributeValue.fromS(claimId)))
                .updateExpression("SET #s = :s")
                .expressionAttributeNames(Map.of("#s", "status"))
                .expressionAttributeValues(
                        Map.of(":s", AttributeValue.fromS(status))));
        log.info("Decision: {}", status);
    }

    private String extractClaimId(String body) {
        try {
            ObjectMapper mapper = new ObjectMapper();

            // Parse SNS envelope
            JsonNode envelope = mapper.readTree(body);

            // Extract inner message
            String message = envelope.get("Message").asText();

            // Parse actual event
            JsonNode event = mapper.readTree(message);

            return event.get("claimId").asText();

        } catch (Exception e) {
            throw new RuntimeException("Failed to extract claimId from message", e);
        }
    }

}

package com.ycompany.claimservice.service;

import java.util.Map;
import java.util.UUID;

import com.ycompany.claimservice.security.Role;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.GetItemResponse;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;

@Slf4j
@Service
public class ClaimService {

    private final DynamoDbClient dynamoDb;
    private final SnsClient sns;
    private final AuditService auditService;

    @Value("${claims.table}")
    private String tableName;

    @Value("${claims.topicArn}")
    private String topicArn;

    public ClaimService(DynamoDbClient dynamoDb,
                        SnsClient sns,
                        AuditService auditService) {
        this.dynamoDb = dynamoDb;
        this.sns = sns;
        this.auditService = auditService;
    }

    private void enforceClaimLimit(String customerId) {

        var response = dynamoDb.query(r -> r
                .tableName(tableName)
                .indexName("customerId-index")
                .keyConditionExpression("customerId = :c")
                .expressionAttributeValues(Map.of(
                        ":c", AttributeValue.fromS(customerId)
                ))
        );

        if (response.count() >= 2) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Not allowed: maximum active claims limit reached"
            );

        }
    }


    public String createClaim(String customerId, boolean isAdmin) {

        if (!isAdmin) {
            enforceClaimLimit(customerId);
        }

        String claimId = UUID.randomUUID().toString();

        // 1️⃣ Persist claim
        dynamoDb.putItem(
                PutItemRequest.builder()
                        .tableName(tableName)
                        .item(Map.of(
                                "claimId", AttributeValue.fromS(claimId),
                                "status", AttributeValue.fromS("CREATED"),
                                "customerId", AttributeValue.fromS(customerId)
                        ))
                        .build()
        );

        // 2️⃣ Publish event
        sns.publish(
                PublishRequest.builder()
                        .topicArn(topicArn)
                        .message("""
                  {
                    "eventType": "CLAIM_SUBMITTED",
                    "claimId": "%s"
                  }
                """.formatted(claimId))
                        .build()
        );

        // 3️⃣ Audit (NON-BLOCKING)
        auditService.log(
                "CREATE_CLAIM",
                "CLAIM",
                claimId
        );

        return claimId;
    }

    public Map<String, String> getClaim(String claimId) {

        GetItemResponse response = dynamoDb.getItem(r -> r
                .tableName(tableName)
                .key(Map.of(
                        "claimId", AttributeValue.fromS(claimId)
                ))
        );

        if (!response.hasItem()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Claim not found"
            );
        }

        Map<String, AttributeValue> item = response.item();

        // ️⃣ Audit successful read
        auditService.log(
                "READ_CLAIM",
                "CLAIM",
                claimId
        );

        auditService.log("READ_CLAIM", "CLAIM", claimId);
        return Map.of(
                "claimId", item.get("claimId").s(),
                "status", item.get("status").s()
        );
    }
}

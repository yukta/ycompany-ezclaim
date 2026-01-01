package com.ycompany.claimservice.service;

import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;

@Service
public class ClaimService {

    private final DynamoDbClient dynamoDb;
    private final SnsClient sns;

    @Value("${claims.table}")
    private String tableName;

    @Value("${claims.topicArn}")
    private String topicArn;

    public ClaimService(DynamoDbClient dynamoDb, SnsClient sns) {
        this.dynamoDb = dynamoDb;
        this.sns = sns;
    }

    public String createClaim() {
        String claimId = UUID.randomUUID().toString();

        dynamoDb.putItem(PutItemRequest.builder()
                .tableName(tableName)
                .item(Map.of(
                        "claimId", AttributeValue.fromS(claimId),
                        "status", AttributeValue.fromS("CREATED")
                ))
                .build());

        sns.publish(PublishRequest.builder()
                .topicArn(topicArn)
                .message("""
          {
            "eventType": "CLAIM_SUBMITTED",
            "claimId": "%s"
          }
        """.formatted(claimId))
                .build());

        return claimId;
    }
}

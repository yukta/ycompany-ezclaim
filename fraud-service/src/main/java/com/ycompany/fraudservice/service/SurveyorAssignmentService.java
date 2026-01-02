package com.ycompany.fraudservice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@Service
@Slf4j
public class SurveyorAssignmentService {

    private static final List<String> SURVEYORS = List.of(
            "surveyor-1",
            "surveyor-2",
            "surveyor-3"
    );

    private final DynamoDbClient dynamo;

    @Value("${fraud.claimsTable}")
    private String table;

    public SurveyorAssignmentService(DynamoDbClient dynamo) {
        this.dynamo = dynamo;
    }

    public void assignSurveyor(String claimId) {

        String surveyorId =
                SURVEYORS.get(ThreadLocalRandom.current().nextInt(SURVEYORS.size()));

        dynamo.updateItem(r -> r
                .tableName(table)
                .key(Map.of("claimId", AttributeValue.fromS(claimId)))
                .updateExpression("SET assignedSurveyorId = :s")
                .expressionAttributeValues(Map.of(
                        ":s", AttributeValue.fromS(surveyorId)
                ))
        );

        log.info("Assigned surveyor {} to claim {}", surveyorId, claimId);
    }
}

package com.ycompany.claimservice.service;

import com.ycompany.claimservice.security.Role;
import com.ycompany.claimservice.security.RoleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Service
public class AuditService {

    private static final Logger log = LoggerFactory.getLogger(AuditService.class);

    private final DynamoDbClient dynamoDb;
    private final String tableName;

    public AuditService(DynamoDbClient dynamoDb,
                        @Value("${audit.table}") String tableName) {
        this.dynamoDb = dynamoDb;
        this.tableName = tableName;
    }

    public void log(String action, String entityType, String entityId) {

        try {
            Role role = RoleContext.get();

            dynamoDb.putItem(r -> r
                    .tableName(tableName)
                    .item(Map.of(
                            "auditId", AttributeValue.fromS(UUID.randomUUID().toString()),
                            "timestamp", AttributeValue.fromS(Instant.now().toString()),
                            "actorRole", AttributeValue.fromS(role.name()),
                            "action", AttributeValue.fromS(action),
                            "entityType", AttributeValue.fromS(entityType),
                            "entityId", AttributeValue.fromS(entityId)
                    ))
            );

        } catch (Exception e) {
            // NON-BLOCKING BY DESIGN
            log.warn("Audit logging failed: {}", e.getMessage());
        }
    }
}

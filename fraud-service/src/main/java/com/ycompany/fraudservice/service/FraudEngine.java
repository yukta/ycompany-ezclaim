package com.ycompany.fraudservice.service;

import com.ycompany.fraudservice.model.FraudDecision;
import org.springframework.stereotype.Component;

@Component
public class FraudEngine {

    public FraudDecision evaluate(String claimId) {
        int score = Math.abs(claimId.hashCode()) % 100;

        if (score > 70) return FraudDecision.HIGH_RISK;
        if (score > 30) return FraudDecision.MEDIUM_RISK;
        return FraudDecision.LOW_RISK;
    }
}


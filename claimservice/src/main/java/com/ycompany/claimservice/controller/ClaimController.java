package com.ycompany.claimservice.controller;

import java.util.Map;

import com.ycompany.claimservice.security.Authorization;
import com.ycompany.claimservice.security.Role;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ycompany.claimservice.service.ClaimService;

@RestController
@RequestMapping("/claims")
public class ClaimController {

    private final ClaimService service;

    public ClaimController(ClaimService service) {
        this.service = service;
    }

    @PostMapping
    public Map<String, String> createClaim() {

        // only a customer or administrator can create the claim
        Authorization.require(
                Role.CUSTOMER,
                Role.ADMINISTRATOR
        );

        return Map.of("claimId", service.createClaim());
    }
}

package com.ycompany.claimservice.controller;

import java.util.Map;

import com.ycompany.claimservice.security.Authorization;
import com.ycompany.claimservice.security.Role;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ycompany.claimservice.service.ClaimService;

@Slf4j
@RestController
@RequestMapping("/claims")
public class ClaimController {

    private final ClaimService service;

    public ClaimController(ClaimService service) {
        this.service = service;
    }

    @PostMapping
    public Map<String, String> createClaim(
            @RequestHeader("X-USER-ID") String userId) {

        Authorization.require(
                Role.CUSTOMER,
                Role.ADMINISTRATOR
        );

        return Map.of(
                "claimId",
                service.createClaim(userId, Authorization.hasRole(Role.ADMINISTRATOR))
        );
    }

    @GetMapping("/{claimId}")
    public Map<String, String> getClaim(@PathVariable String claimId) {

        Authorization.require(
                Role.SURVEYOR,
                Role.ADMINISTRATOR,
                Role.CUSTOMER
        );

        return service.getClaim(claimId);
    }
}
